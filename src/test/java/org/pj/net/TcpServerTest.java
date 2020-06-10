package org.pj.net;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.Assert;
import org.junit.Test;
import org.pj.msg.proto.MessageProto.Message;
import org.pj.net.init.SocketHandler;
import org.pj.net.init.WebSocketHandler;

public class TcpServerTest {

  @Test
  public void webSocketTest() throws Exception {
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(1)
        .setStat(200)
        .setSerial(0)
        .setBody(ByteString.copyFromUtf8("Hello, WebSocket World!!!!")).build();

    TcpServer server = new TcpServer(8080);
    server.startUp(new WebSocketHandler(new SimpleChannelInboundHandler<Message>() {
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
    WebSocketClient client = new EchoWebSocketClient(new URI("ws://127.0.0.1:8080"), latch) {
      @Override
      public void onMessage(ByteBuffer bytes) {
        try {
          Message echoMessage = Message.parseFrom(bytes);
          Assert.assertEquals(message, echoMessage);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        latch.countDown();
      }
    };
    client.connectBlocking();
    client.send(message.toByteArray());

    boolean suc = latch.await(1, TimeUnit.MINUTES);
    server.close();

    Assert.assertTrue("Echo Failed", suc);
  }

  @Test
  public void socketTest() throws Exception {
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(1)
        .setStat(200)
        .setSerial(0)
        .setBody(ByteString.copyFromUtf8("Hello, Socket World!!!!")).build();

    TcpServer server = new TcpServer(8080);
    server.startUp(new SocketHandler(new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        ctx.write(msg.retain());
      }

      public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
      }
    }));

    int loop = 5;
    CountDownLatch latch = new CountDownLatch(loop);
    ExampleTcpClient client = new ExampleTcpClient("localhost", 8080,
        new SocketHandler(new SimpleChannelInboundHandler<ByteBuf>() {
          @Override
          public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
              throws Exception {
            Message echoMessage = Message.parseFrom(msg.nioBuffer());
            System.out.println(echoMessage.getBody().toStringUtf8());
            latch.countDown();

            Assert.assertEquals(message, echoMessage);
          }
        }));

    for (int i = 0; i < loop; i++) {
      client.sendMsg(message);
    }

    boolean suc = latch.await(1, TimeUnit.MINUTES);
    server.close();

    Assert.assertTrue("Echo Failed", suc);
  }

}
