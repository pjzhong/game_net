package org.pj.core.net;

import static io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE;

import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.common.NamedThreadFactory;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketClientHandlerInitializer;
import org.pj.core.net.init.WebSocketServerHandlerInitializer;

public class TcpClientTest {

  private static NettyTcpServer server;
  private static NettyTcpServer webSocketServer;

  @BeforeClass
  public static void start() throws Exception {
    server = new NettyTcpServer(8080);
    server.startUp(new ProtobufSocketHandlerInitializer(
        new SimpleChannelInboundHandler<Message>() {
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            ctx.write(msg);
          }

          @Override
          public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
          }
        }));

    webSocketServer = new NettyTcpServer(8081);
    webSocketServer.startUp(new WebSocketServerHandlerInitializer(
        new SimpleChannelInboundHandler<Message>() {
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            ctx.write(msg);
          }

          @Override
          public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
          }
        }));
  }

  @AfterClass
  public static void close() {
    server.close();
    webSocketServer.close();
  }


  @Test
  public void echoHelloWorld() throws Exception {
    Message message = Message.newBuilder().setBody(ByteString.copyFromUtf8("Hello World")).build();
    CountDownLatch latch = new CountDownLatch(1);
    ChannelHandler handler = new ProtobufSocketHandlerInitializer(
        new SimpleChannelInboundHandler<Message>() {
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            Assert.assertEquals(msg, message);
            latch.countDown();
          }
        });

    NettyTcpClient client = new NettyTcpClient(server.getBootstrap().config().group());
    client.connect(new InetSocketAddress("127.0.0.1", 8080), handler);
    client.sendMessage(message);

    Assert.assertTrue("Echo failed", latch.await(1, TimeUnit.SECONDS));

    client.close();
  }

  @Test
  public void echoOnWebSocket() throws Exception {
    Message message = Message.newBuilder().setModule(2)
        .setBody(ByteString.copyFromUtf8("Hello World")).build();
    CountDownLatch latch = new CountDownLatch(1);
    CountDownLatch wait = new CountDownLatch(1);

    URI uri = URI.create("ws://127.0.0.1:8081/");
    ChannelHandler handler = new WebSocketClientHandlerInitializer(
        uri,
        new SimpleChannelInboundHandler<Message>() {

          @Override
          public void userEventTriggered(ChannelHandlerContext ctx, Object env) {
            if (env == HANDSHAKE_COMPLETE) {
              System.out.println(env);
              wait.countDown();
            }
          }

          @Override
          protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            Assert.assertEquals(msg, message);
            latch.countDown();
          }
        });

    EventLoopGroup group = new NioEventLoopGroup(1, new NamedThreadFactory("svr-client"));
    NettyTcpClient client = new NettyTcpClient(group);
    client.connect(new InetSocketAddress(uri.getHost(), uri.getPort()), handler);
    wait.await(1, TimeUnit.SECONDS);
    client.sendMessage(message);

    Assert.assertTrue("Echo failed", latch.await(1, TimeUnit.SECONDS));
  }

}
