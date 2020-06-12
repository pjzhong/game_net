package org.pj.core.msg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.ExampleTcpClient;
import org.pj.core.net.TcpServer;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.ProtobufSocketHandler;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

public class MessageDispatcherTest {

  private static TcpServer tcpServer;
  private static MessageDispatcher dispatcher;

  @BeforeClass
  public static void init() throws Exception {
    MessageDispatcher messageDispatcher = new MessageDispatcher(
        Runtime.getRuntime().availableProcessors());

    messageDispatcher.registerHandler(new SimpleFacadeFacade());
    Assert.assertFalse(messageDispatcher.getHandlers().isEmpty());

    TcpServer server = new TcpServer(8080);
    server.startUp(new ProtobufSocketHandler(new MessageHandler(messageDispatcher)));

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
        new ProtobufSocketHandler(new SimpleChannelInboundHandler<Message>() {
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
        new ProtobufSocketHandler(new SimpleChannelInboundHandler<Message>() {
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

    int loop = 1000;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandler(new SimpleChannelInboundHandler<Message>() {
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

    Assert.assertTrue("Echo Failed", latch.await(300, TimeUnit.MILLISECONDS));
  }

  public static class SimpleFacadeFacade {

    @Packet(1)
    public Message HelloWorld() {
      return Message.newBuilder().setBody(ByteString.copyFromUtf8("HelloWorld")).build();
    }


    @Packet(2)
    public Message echoContext(Channel channel, Message message) {
      Assert.assertNotNull(channel);
      return message;
    }

    @Packet(3)
    public HelloWorld echoHelloWorld(Channel channel, HelloWorld world) {
      Assert.assertNotNull(channel);
      return world;
    }

    public HelloWorld noEffect(HelloWorld world) {
      return world;
    }

  }

}
