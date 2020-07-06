package org.pj.core.msg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.ExampleWebSocketClient;
import org.pj.core.net.NettyTcpServer;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.WebSocketServerHandlerInitializer;
import org.pj.protocols.hello.HelloFacade;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

public class WebSocketMessageDispatcherTest {

  private static NettyTcpServer tcpServer;
  private static MessageDispatcher dispatcher;

  @BeforeClass
  public static void init() throws Exception {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors() * 2);

    messageDispatcher.registerHandler(new HelloFacade());
    Assert.assertFalse(messageDispatcher.getHandlers().isEmpty());

    NettyTcpServer server = new NettyTcpServer(8080);
    server.startUp(new WebSocketServerHandlerInitializer(new MessageHandler(messageDispatcher)));

    tcpServer = server;
    dispatcher = messageDispatcher;
  }

  @AfterClass
  public static void close() {
    dispatcher.close();
    tcpServer.close();

    dispatcher = null;
    tcpServer = null;
  }

  private ExampleWebSocketClient newClient(Consumer<Message> consumer)
      throws Exception {
    ExampleWebSocketClient client = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080")) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          consumer.accept(Message.parseFrom(bytes));
        } catch (InvalidProtocolBufferException e) {
          throw new RuntimeException(e);
        }
      }
    };
    client.connectBlocking();
    return client;
  }

  @Test
  public void helloWorldTest() throws Exception {

    Message request = Message.newBuilder().setModule(1).build();

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleWebSocketClient client = newClient(msg -> {
      Assert.assertEquals("HelloWorld", msg.getBody().toStringUtf8());
      latch.countDown();
    });
    for (int i = 0; i < loop; i++) {
      client.send(request.toByteArray());
    }

    Assert.assertTrue("HelloWorld Failed", latch.await(300, TimeUnit.MILLISECONDS));

    client.close();
  }

  @Test
  public void echoContextTest() throws Exception {

    Message request = Message.newBuilder().setModule(2).setBody(ByteString.copyFromUtf8("echo"))
        .build();

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleWebSocketClient client = newClient(msg -> {
      Assert.assertEquals(request.getBody(), msg.getBody());
      Assert.assertEquals(-request.getModule(), msg.getModule());
      Assert.assertEquals(200, msg.getStat());
      latch.countDown();
    });

    for (int i = 0; i < loop; i++) {
      client.send(request.toByteArray());
    }

    Assert.assertTrue("Echo Failed", latch.await(300, TimeUnit.MILLISECONDS));
    client.close();
  }

  @Test
  public void echoHelloWorld() throws Exception {

    HelloWorld world = HelloWorld.newBuilder().setStr("echoHelloWorld").build();
    Message request = Message.newBuilder().setModule(3)
        .setBody(world.toByteString())
        .build();

    int loop = 10;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleWebSocketClient client = newClient(msg -> {
      HelloWorld echoWorld = null;
      try {
        echoWorld = HelloWorld.parseFrom(msg.getBody());
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
      Assert.assertEquals(request.getBody(), msg.getBody());
      Assert.assertEquals(world, echoWorld);
      latch.countDown();
    });

    for (int i = 0; i < loop; i++) {
      client.send(request.toByteArray());
    }

    Assert.assertTrue("Echo Failed", latch.await(1, TimeUnit.SECONDS));

    client.close();
  }

  @Test
  public void countTest() throws Exception {
    Message request = Message.newBuilder().setModule(4)
        .build();

    int size = 100, senders = 8, total = size * senders;
    CountDownLatch latch = new CountDownLatch(total);
    int[] result = new int[total + 1];
    ExampleWebSocketClient[] clients = new ExampleWebSocketClient[senders];
    for (int i = 0; i < senders; i++) {
      clients[i] = newClient(msg -> {
        HelloWorld echoWorld = null;
        try {
          echoWorld = HelloWorld.parseFrom(msg.getBody());
          result[echoWorld.getCount()] = echoWorld.getCount();
          latch.countDown();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }

    ForkJoinPool pool = ForkJoinPool.commonPool();
    Random random = ThreadLocalRandom.current();
    for (int i = 0, loop = size * senders, limits = senders - 1; i < loop; i++) {
      int h = random.nextInt();
      int idx = (h ^ (h >>> 16)) & limits;
      pool.execute(() -> clients[idx].send(request.toByteArray()));
    }

    Assert.assertTrue("Count TimeOut", latch.await(10, TimeUnit.SECONDS));
    for (int i = 1; i <= total; i++) {
      Assert.assertEquals(i, result[i]);
    }

    System.out.println(Arrays.toString(result));

    for (ExampleWebSocketClient c : clients) {
      c.close();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void duplicatedRegister() {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors());

    messageDispatcher.registerHandler(new HelloFacade());
    messageDispatcher.registerHandler(new HelloFacade());
  }

}
