package org.pj.core.net;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.pj.core.msg.Message;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketServerHandlerInitializer;

public class TcpServerTest {

  @Test
  public void webSocketTest() throws Exception {
    Message message = Message.valueOf()
        .setModule(1)
        .setStates(200)
        .setOpt(0)
        .setBody("Hello, WebSocket World!!!!".getBytes(StandardCharsets.UTF_8));

    NettyTcpServer server = new NettyTcpServer(8080);
    server
        .startUp(new WebSocketServerHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            ctx.write(msg);
          }

          @Override
          public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
          }
        }));

    CountDownLatch latch = new CountDownLatch(1);
    WebSocketClient client = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080")) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        Message echoMessage = Message.readFrom(bytes);
        assertArrayEquals(message.getBody(), echoMessage.getBody());
        latch.countDown();
      }
    };
    client.connectBlocking();
    client.send(message.toByteArray());

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    server.close();
    client.close();

    assertTrue(suc, "Echo Failed");
  }

  @Test
  public void socketTest() throws Exception {
    Message message = Message.valueOf()
        .setModule(1)
        .setStates(200)
        .setOpt(0)
        .setBody("Hello, Socket World!!!!".getBytes(StandardCharsets.UTF_8));

    NettyTcpServer server = new NettyTcpServer(8080);
    server.startUp(new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        ctx.write(msg);
      }

      public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
      }
    }));

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new ProtobufSocketHandlerInitializer(new SimpleChannelInboundHandler<Message>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, Message msg) {
            assertArrayEquals(message.getBody(), msg.getBody());
            latch.countDown();
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(message);
    }

    boolean suc = latch.await(1, TimeUnit.SECONDS);
    server.close();
    client.close();

    assertTrue(suc, "Echo Failed");
  }

}
