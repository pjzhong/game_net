package org.pj.core.msg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.pj.common.NamedThreadFactory;
import org.pj.core.net.ExampleTcpClient;
import org.pj.core.net.NettyTcpServer;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.protocols.hello.HelloFacade;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

public class MessageDispatcherTest {

  private static NettyTcpServer tcpServer;
  private static MessageDispatcher dispatcher;

  @BeforeAll
  public static void init() throws Exception {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors() * 2);

    messageDispatcher.registerHandler(new HelloFacade());
    assertFalse(messageDispatcher.getHandlers().isEmpty());

    NettyTcpServer server = new NettyTcpServer(8080);
    server.startUp(new ProtobufSocketHandlerInitializer(new MessageHandler(messageDispatcher)));

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

  @RepeatedTest(10)
  public void helloWorldTest() throws Exception {

    Message request = Message.valueOf().setModule(2)
        .setBody(ByteString.copyFromUtf8("HelloWorld").toByteArray());
    Message request1 = Message.valueOf().setModule(2)
        .setBody(ByteString.copyFromUtf8("HelloWorld1").toByteArray());


    int loop = 100;
    CountDownLatch latch = new CountDownLatch(loop * 2);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg) {
            assertEquals(new String(msg.getBody()), "HelloWorld");
            latch.countDown();
          }
        }));
    ExampleTcpClient client1 = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg) {
            assertEquals(new String(msg.getBody()), "HelloWorld1");
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(request);
      client1.sendMsg(request1);
    }

    assertTrue(latch.await(1, TimeUnit.MINUTES), "HelloWorld Failed");
    client.close();
    client1.close();
  }

  @Test
  public void echoContextTest() throws Exception {
    Message request = new Message().setModule(2).setBody("echo".getBytes(StandardCharsets.UTF_8));

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg) {
            assertArrayEquals(request.getBody(), msg.getBody());
            assertEquals(-request.getModule(), msg.getModule());
            assertEquals(200, msg.getOpt());
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(request);
    }

    assertTrue(latch.await(300, TimeUnit.MINUTES), "Echo Failed");
  }

  @Test
  public void echoHelloWorld() throws Exception {

    HelloWorld world = HelloWorld.newBuilder().setStr("echoHelloWorld").build();
    Message request = Message.valueOf().setModule(3)
        .setBody(world.toByteArray());

    int loop = 100;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg)
              throws InvalidProtocolBufferException {
            HelloWorld echoWorld = HelloWorld.parseFrom(msg.getBody());
            assertArrayEquals(request.getBody(), msg.getBody());
            assertEquals(world, echoWorld);
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(request);
    }

    assertTrue(latch.await(1, TimeUnit.SECONDS), "Echo Failed");
  }

  @Test
  public void countTcpTest() throws Exception {
    Message request = Message.valueOf().setModule(4);

    int size = 100, senders = 8, total = size * senders;
    CountDownLatch latch = new CountDownLatch(total);
    int[] result = new int[total + 1];
    EventLoopGroup group = new NioEventLoopGroup(4, new NamedThreadFactory("count_client"));
    ExampleTcpClient[] clients = new ExampleTcpClient[senders];
    for (int i = 0; i < senders; i++) {
      clients[i] = new ExampleTcpClient("localhost", 8080, group,
          new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
            @Override
            public void channelRead0(ChannelHandlerContext ctx, Message msg)
                throws InvalidProtocolBufferException {
              HelloWorld echoWorld = HelloWorld.parseFrom(msg.getBody());
              result[echoWorld.getCount()] = echoWorld.getCount();
              latch.countDown();
            }
          }));
    }

    ForkJoinPool pool = ForkJoinPool.commonPool();
    Random random = ThreadLocalRandom.current();
    for (int i = 0, loop = size * senders, limits = senders - 1; i < loop; i++) {
      int h = random.nextInt();
      int idx = (h ^ (h >>> 16)) & limits;
      pool.execute(() -> clients[idx].sendMsg(request));
    }

    assertTrue(latch.await(1, TimeUnit.MINUTES));
    for (int i = 1; i <= total; i++) {
      assertEquals(i, result[i]);
    }

    System.out.println(Arrays.toString(result));

    for (ExampleTcpClient c : clients) {
      c.close();
    }
  }

  @Test
  public void duplicatedRegister() {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors());

    messageDispatcher.registerHandler(new HelloFacade());
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> messageDispatcher.registerHandler(new HelloFacade()));
  }

}
