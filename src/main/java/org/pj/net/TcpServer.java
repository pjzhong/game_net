package org.pj.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.pj.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer {

  private final int port;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ServerBootstrap bootstrap;


  public TcpServer(int port) {
    this.port = port;
  }

  public void startUp(ChannelInitializer initializer) throws Exception {
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
    bootstrap.childHandler(initializer);


    bootstrap.bind(port).sync().await();

    logger.info("Tcp server, start at:{}", port);
  }
}
