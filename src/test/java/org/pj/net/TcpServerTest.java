package org.pj.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.junit.Test;
import org.pj.net.init.WebSocketHandler;

public class TcpServerTest {

  @Test
  public void startTest() throws Exception {
    TcpServer server = new TcpServer(8080);
    server.startUp(new WebSocketHandler(new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(msg);
        ctx.writeAndFlush(buf);
      }
    }));

    WebSocketClient client = new ExampleClient(new URI("ws://127.0.0.1:8080"));
    client.connectBlocking();
    client.send("Hello World".getBytes(StandardCharsets.UTF_8));

    TimeUnit.MILLISECONDS.sleep(1);
  }

}
