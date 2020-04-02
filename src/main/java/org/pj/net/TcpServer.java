package org.pj.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.pj.common.NamedThreadFactory;
import org.pj.net.handler.WebSocketDecoder;
import org.pj.net.handler.WebSocketEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer {

  private final int port;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ServerBootstrap bootstrap;


  public TcpServer(int port) {
    this.port = port;
  }

  public void startUp(ChannelHandler handler) throws Exception {
    bootstrap = new ServerBootstrap();
    NioEventLoopGroup boss = new NioEventLoopGroup(0, new NamedThreadFactory("netty-boss"));
    NioEventLoopGroup worker = new NioEventLoopGroup(0, new NamedThreadFactory("netty-worker"));

    bootstrap.group(boss, worker);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    bootstrap.childOption(ChannelOption.SO_RCVBUF, 128 * 1024);
    bootstrap.childOption(ChannelOption.SO_SNDBUF, 128 * 1024);

    bootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
  }

  static class WebSocketHandler extends ChannelInitializer {

    private ChannelHandler handler;

    public WebSocketHandler(ChannelHandler handler) {
      this.handler = handler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
      ChannelPipeline pip = ch.pipeline();

      pip.addLast(new HttpServerCodec());
      pip.addLast(new HttpObjectAggregator(65536));
      pip.addLast(new WebSocketServerCompressionHandler());
      pip.addLast(new WebSocketServerProtocolHandler("/"));
      pip.addLast(new WebSocketDecoder());
      pip.addLast(new WebSocketEncoder());
      //TODO My Message Decoder
      pip.addLast(handler);//TODO MY Message Handler

    }
  }


}
