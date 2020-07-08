package org.pj.core.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketServerHandlerInitializer;

public class TcpServerTest {

  @Test
  public void webSocketTest() throws Exception {
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(1)
        .setStat(200)
        .setSerial(0)
        .setBody(ByteString.copyFromUtf8("Hello, WebSocket World!!!!")).build();

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
    WebSocketClient client = new ExampleWebSocketClient(new URI("ws://127.0.0.1:8080"), latch) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          Message echoMessage = Message.parseFrom(bytes);
          assertEquals(message, echoMessage);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
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
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(1)
        .setStat(200)
        .setSerial(0)
        .setBody(ByteString.copyFromUtf8("Hello, Socket World!!!!")).build();

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
            System.out.println(msg.getBody().toStringUtf8());
            assertEquals(message, msg);
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
