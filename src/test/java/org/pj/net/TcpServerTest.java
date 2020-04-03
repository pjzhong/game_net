package org.pj.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.Assert;
import org.junit.Test;
import org.pj.net.ExampleTcpClient.ChatClientInitializer;
import org.pj.net.init.SocketHandler;
import org.pj.net.init.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServerTest {

  @Test
  public void webSocketTest() throws Exception {
    TcpServer server = new TcpServer(8080);
    server.startUp(new WebSocketHandler(new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        ctx.writeAndFlush(msg.retain());
      }
    }));


    CountDownLatch latch = new CountDownLatch(1);
    String hello = "Hello, WebSocket World";
    WebSocketClient client = new EchoWebSocketClient(new URI("ws://127.0.0.1:8080"), latch);
    client.connectBlocking();
    client.send(hello.getBytes(StandardCharsets.UTF_8));

    boolean suc = latch.await(1, TimeUnit.MINUTES);
    server.close();

    Assert.assertTrue("Echo Failed", suc);
  }

  @Test
  public void socketTest() throws Exception {
    TcpServer server = new TcpServer(8080);
    server.startUp(new SocketHandler(new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        ctx.writeAndFlush(msg.retain());
      }
    }));

    String helloWorld = "Hello, Socket World";
    CountDownLatch latch = new CountDownLatch(1);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ChatClientInitializer(new ChannelInboundHandlerAdapter() {
          @Override
          public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            ByteBuf res = Unpooled.buffer();
            res.writeBytes((ByteBuf) msg);
            logger.info(new String(res.array()));
            latch.countDown();
          }
        }));
    client.sendMsg(helloWorld);

    boolean suc = latch.await(1, TimeUnit.MINUTES);
    server.close();

    Assert.assertTrue("Echo Failed", suc);
  }

}
