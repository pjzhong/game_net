package org.pj.core.framework;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pj.common.hello.HelloWorldProto.HelloWorld;
import org.pj.config.GameServerConfig;
import org.pj.core.msg.Message;
import org.pj.core.net.ExampleWebSocketClient;
import org.pj.core.net.NettyTcpClient;
import org.pj.core.net.init.WebSocketClientHandlerInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

@SpringBootTest(classes = GameServerConfig.class)
@TestExecutionListeners(GameContextTest.class)
public class GameContextTest implements TestExecutionListener {

  private static SpringGameContext ctx;

  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    ctx = testContext.getApplicationContext().getBean(SpringGameContext.class);
    ctx.start();
  }

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
    ctx.close();
  }

  @Test
  public void echoHelloWorld() throws Exception {
    if (ctx.getEnvironment().getProperty("game.isSocket", Boolean.class, false)) {
      return;
    }

    HelloWorld world = HelloWorld.newBuilder().setStr("Hello World").build();
    Message message = Message.valueOf()
        .setModule(2)
        .setStates(200)
        .setOpt(0)
        .setBody(HelloWorld.newBuilder().setStr("Hello World").build().toByteArray());

    int loop = 100;
    CountDownLatch latch = new CountDownLatch(loop * 2);
    WebSocketClient client1 = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080"), latch) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          Message echoMessage = Message.readFrom(bytes);
          HelloWorld echoWorld = HelloWorld.parseFrom(echoMessage.getBody());
          Assertions.assertEquals(echoWorld, world);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        latch.countDown();
      }
    };
    WebSocketClient client2 = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080"), latch) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          Message echoMessage = Message.readFrom(bytes);
          HelloWorld echoWorld = HelloWorld.parseFrom(echoMessage.getBody());
          Assertions.assertEquals(echoWorld, world);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        latch.countDown();
      }
    };

    client1.connectBlocking();
    client2.connectBlocking();

    for (int i = 0; i < loop; i++) {
      client1.send(message.toByteArray());
      client2.send(message.toByteArray());
    }

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    Assertions.assertTrue(suc, "Echo Failed");

    client2.close();
    client1.close();
  }

  @Test
  public void testErr() throws Exception {
    if (ctx.getEnvironment().getProperty("game.isSocket", Boolean.class, false)) {
      return;
    }

    Message message = Message.valueOf()
        .setModule(5)
        .setStates(200)
        .setOpt(0)
        .setBody(HelloWorld.newBuilder().setStr("Hello World").build().toByteArray());

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
                Assertions.assertEquals(-5, msg.getModule());
                Assertions.assertEquals(500, msg.getStates());
                latch.countDown();
              }
            }));

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    Assertions.assertTrue(suc, "test err-response Failed");

    group.shutdownGracefully();
    client.close();
  }

}
