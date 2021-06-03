package org.pj.core.msg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pj.common.hello.HelloFacade;
import org.pj.common.hello.HelloWorldProto.HelloWorld;
import org.pj.core.net.ExampleWebSocketClient;
import org.pj.core.net.NettyTcpServer;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.WebSocketServerHandlerInitializer;

public class WebSocketMessageDispatcherTest {

  private static NettyTcpServer tcpServer;
  private static MessageDispatcher dispatcher;

  @BeforeAll
  public static void init() throws Exception {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors() * 2);

    messageDispatcher.registerHandler(new HelloFacade());
    assertFalse(messageDispatcher.getHandlers().isEmpty());

    NettyTcpServer server = new NettyTcpServer(8080);
    server.startUp(new WebSocketServerHandlerInitializer(new MessageHandler(messageDispatcher)));

    tcpServer = server;
    dispatcher = messageDispatcher;
  }

  @AfterAll
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
        consumer.accept(Message.readFrom(bytes));

      }
    };
    client.connectBlocking();
    return client;
  }

  @Test
  public void helloWorldTest() throws Exception {

    Message request = Message.valueOf().setModule(1);

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleWebSocketClient client = newClient(msg -> {
      assertEquals("HelloWorld", new String(msg.getBody()));
      latch.countDown();
    });
    for (int i = 0; i < loop; i++) {
      client.send(request.toByteArray());
    }

    assertTrue(latch.await(300, TimeUnit.MILLISECONDS), "HelloWorld Failed");

    client.close();
  }

  @Test
  public void echoContextTest() throws Exception {

    Message request = Message.valueOf().setModule(2)
        .setBody(ByteString.copyFromUtf8("echo").toByteArray());

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleWebSocketClient client = newClient(msg -> {
      assertArrayEquals(request.getBody(), msg.getBody());
      assertEquals(-request.getModule(), msg.getModule());
      assertEquals(200, msg.getStates());
      latch.countDown();
    });

    for (int i = 0; i < loop; i++) {
      client.send(request.toByteArray());
    }

    assertTrue(latch.await(300, TimeUnit.MILLISECONDS), "Echo Failed");
    client.close();
  }

  @Test
  public void echoHelloWorld() throws Exception {

    HelloWorld world = HelloWorld.newBuilder().setStr("echoHelloWorld").build();
    Message request = Message.valueOf().setModule(2)
        .setBody(world.toByteArray())
        ;

    int loop = 10;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleWebSocketClient client = newClient(msg -> {
      HelloWorld echoWorld = null;
      try {
        echoWorld = HelloWorld.parseFrom(msg.getBody());
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
      assertArrayEquals(request.getBody(), msg.getBody());
      assertEquals(world, echoWorld);
      latch.countDown();
    });

    for (int i = 0; i < loop; i++) {
      client.send(request.toByteArray());
    }

    assertTrue(latch.await(1, TimeUnit.SECONDS), "Echo Failed");

    client.close();
  }

  @Test
  public void countTest() throws Exception {
    Message request = Message.valueOf().setModule(4);

    int size = 10, senders = 10, total = size * senders;
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

    byte[] array = request.toByteArray();
    for (int i = 0; i < size; i++) {
      for (int s = 0; s < senders; s++) {
        clients[s].send(array);
      }
    }

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Count TimeOut");
    for (int i = 1; i <= total; i++) {
      assertEquals(i, result[i]);
    }

    System.out.println(Arrays.toString(result));

    for (ExampleWebSocketClient c : clients) {
      c.close();
    }
  }

}
