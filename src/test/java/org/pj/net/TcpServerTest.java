package org.pj.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.junit.Test;
import org.pj.net.init.WebSocketHandler;

public class TcpServerTest {

  @Test
  public void startTest() throws Exception {
    TcpServer server = new TcpServer(8080);
    server.startUp(new WebSocketHandler(new SimpleChannelInboundHandler<>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.writeAndFlush("Hello, World");
      }
    }));
  }

}
