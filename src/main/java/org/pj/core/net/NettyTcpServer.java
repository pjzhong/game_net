package org.pj.core.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyTcpServer implements AutoCloseable {

  private final int port;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private ServerBootstrap bootstrap;
  private Channel channel;


  public NettyTcpServer(int port) {
    this.port = port;
  }

  public void startUp(ChannelHandler handler) throws Exception {
    bootstrap = new ServerBootstrap();

    bootstrap.group(ThreadCommon.BOSS, ThreadCommon.WORKER);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    bootstrap.childOption(ChannelOption.SO_RCVBUF, 128 * 1024);
    bootstrap.childOption(ChannelOption.SO_SNDBUF, 128 * 1024);

    bootstrap.handler(new LoggingHandler(LogLevel.INFO));
    bootstrap.childHandler(handler);

    ChannelFuture future = bootstrap.bind(port).sync().await();

    channel = future.channel();

    logger.info("Tcp server, init at:{}", port);
  }

  public ServerBootstrap getBootstrap() {
    return bootstrap;
  }

  @Override
  public void close() {
    if (bootstrap == null) {
      return;
    }
    channel.close();
  }
}
