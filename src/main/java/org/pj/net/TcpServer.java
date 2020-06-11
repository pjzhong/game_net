package org.pj.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.pj.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer implements AutoCloseable {

  private final int port;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ServerBootstrap bootstrap;


  public TcpServer(int port) {
    this.port = port;
  }

  public void startUp(ChannelHandler handler) throws Exception {
    bootstrap = new ServerBootstrap();
    NioEventLoopGroup boss = new NioEventLoopGroup(1, new NamedThreadFactory("svr-boss"));
    NioEventLoopGroup worker = new NioEventLoopGroup(0,
        new NamedThreadFactory("svr-worker"));

    bootstrap.group(boss, worker);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    bootstrap.childOption(ChannelOption.SO_RCVBUF, 128 * 1024);
    bootstrap.childOption(ChannelOption.SO_SNDBUF, 128 * 1024);

    bootstrap.handler(new LoggingHandler(LogLevel.INFO));
    bootstrap.childHandler(handler);

    bootstrap.bind(port).sync().await();

    logger.info("Tcp server, start at:{}", port);
  }

  @Override
  public void close() {
    bootstrap.config().group().shutdownGracefully();
    bootstrap.config().childGroup().shutdownGracefully();
  }
}
