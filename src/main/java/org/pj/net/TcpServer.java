package org.pj.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import org.pj.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer {

  private final int port;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private NioEventLoopGroup boss;
  private NioEventLoopGroup worker;

  public TcpServer(int port) {
    this.port = port;
  }

  public ChannelFuture startUp(ChannelHandler handler) throws Exception {
    ServerBootstrap boot = new ServerBootstrap();
    boss = new NioEventLoopGroup(0, new NamedThreadFactory("netty-boss"));
    worker = new NioEventLoopGroup(0, new NamedThreadFactory("netty-worker"));
    //TODO OTHER HANDLERS
    boot.group(boss, worker);

    ChannelFuture future = boot.bind(port).sync();

    return future;
  }


}
