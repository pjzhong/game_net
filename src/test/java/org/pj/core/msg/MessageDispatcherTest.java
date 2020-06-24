package org.pj.core.msg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.common.NamedThreadFactory;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.ExampleTcpClient;
import org.pj.core.net.TcpServer;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.protocols.hello.HelloFacade;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

public class MessageDispatcherTest {

  private static TcpServer tcpServer;
  private static MessageDispatcher dispatcher;

  @BeforeClass
  public static void init() throws Exception {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors() * 2);

    messageDispatcher.registerHandler(new HelloFacade());
    Assert.assertFalse(messageDispatcher.getHandlers().isEmpty());

    TcpServer server = new TcpServer(8080);
    server.startUp(new ProtobufSocketHandlerInitializer(new MessageHandler(messageDispatcher)));

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

  @Test
  public void helloWorldTest() throws Exception {

    Message request = Message.newBuilder().setModule(1).build();

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg) {
            Assert.assertEquals("HelloWorld", msg.getBody().toStringUtf8());
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(request);
    }

    Assert.assertTrue("HelloWorld Failed", latch.await(300, TimeUnit.MILLISECONDS));
  }

  @Test
  public void echoContextTest() throws Exception {

    Message request = Message.newBuilder().setModule(2).setBody(ByteString.copyFromUtf8("echo"))
        .build();

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg) {
            Assert.assertEquals(request, msg);
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(request);
    }

    Assert.assertTrue("Echo Failed", latch.await(300, TimeUnit.MILLISECONDS));
  }

  @Test
  public void echoHelloWorld() throws Exception {

    HelloWorld world = HelloWorld.newBuilder().setStr("echoHelloWorld").build();
    Message request = Message.newBuilder().setModule(3)
        .setBody(world.toByteString())
        .build();

    int loop = 100;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg)
              throws InvalidProtocolBufferException {
            HelloWorld echoWorld = HelloWorld.parseFrom(msg.getBody());
            Assert.assertEquals(request, msg);
            Assert.assertEquals(world, echoWorld);
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(request);
    }

    Assert.assertTrue("Echo Failed", latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void countTcpTest() throws Exception {
    Message request = Message.newBuilder().setModule(4)
        .build();

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

    Assert.assertTrue("Count TimeOut", latch.await(1, TimeUnit.MINUTES));
    for (int i = 1; i <= total; i++) {
      Assert.assertEquals(i, result[i]);
    }

    System.out.println(Arrays.toString(result));

    for (ExampleTcpClient c : clients) {
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
