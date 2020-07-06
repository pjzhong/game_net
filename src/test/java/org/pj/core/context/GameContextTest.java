package org.pj.core.context;

import static io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.ExampleWebSocketClient;
import org.pj.core.net.NettyTcpClient;
import org.pj.core.net.init.WebSocketClientHandlerInitializer;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class GameContextTest {

  private static SpringGameContext ctx;
  private static GenericApplicationContext context;

  @BeforeClass
  public static void init() throws Exception {
    long start = System.currentTimeMillis();
    context = new AnnotationConfigApplicationContext(ServerConfig.class);
    SpringGameContext gameContext = context.getBean(SpringGameContext.class);
    gameContext.start();

    ctx = gameContext;
    System.out.println("cost:" + (System.currentTimeMillis() - start));
  }

  @AfterClass
  public static void end() throws Exception {
    ctx.close();
    context.close();
  }


  @Test
  public void echoHelloWorld() throws Exception {
    if (context.getEnvironment().getProperty("game.isSocket", Boolean.class, false)) {
      return;
    }

    HelloWorld world = HelloWorld.newBuilder().setStr("Hello World").build();
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(3)
        .setStat(200)
        .setSerial(0)
        .setBody(HelloWorld.newBuilder().setStr("Hello World").build().toByteString()).build();

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    WebSocketClient client = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080"), latch) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          Message echoMessage = Message.parseFrom(bytes);
          HelloWorld echoWorld = HelloWorld.parseFrom(echoMessage.getBody());
          Assert.assertEquals(echoWorld, world);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        latch.countDown();
      }
    };
    client.connectBlocking();

    for (int i = 0; i < loop; i++) {
      client.send(message.toByteArray());
    }

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    Assert.assertTrue("Echo Failed", suc);
  }

  @Test
  public void testErr() throws Exception {
    if (context.getEnvironment().getProperty("game.isSocket", Boolean.class, false)) {
      return;
    }

    Message message = Message.newBuilder()
        .setModule(5)
        .setStat(200)
        .setSerial(0)
        .setBody(HelloWorld.newBuilder().setStr("Hello World").build().toByteString()).build();

    CountDownLatch latch = new CountDownLatch(1);
    NioEventLoopGroup group = new NioEventLoopGroup(1);
    NettyTcpClient client = new NettyTcpClient(group);
    URI uri = URI.create("ws://localhost:8080");
    client.connect(new InetSocketAddress(uri.getHost(), uri.getPort()),
        new WebSocketClientHandlerInitializer(uri,
            new SimpleChannelInboundHandler<Message>() {

              public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                if (evt == HANDSHAKE_COMPLETE) {
                  ctx.writeAndFlush(message);
                }
                ctx.fireUserEventTriggered(evt);
              }

              @Override
              protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                Assert.assertEquals(-5, msg.getModule());
                Assert.assertEquals(100, msg.getStat());
                latch.countDown();
              }
            }));

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    Assert.assertTrue("test err-response Failed", suc);

    group.shutdownGracefully();
    client.close();
  }

}
