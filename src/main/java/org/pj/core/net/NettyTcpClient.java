package org.pj.core.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import org.pj.core.msg.MessageProto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyTcpClient implements AutoCloseable {


  private final EventLoopGroup group;
  private InetSocketAddress address;
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private Channel channel;

  public NettyTcpClient(EventLoopGroup group) {
    this.group = group;
  }

  public void connect(InetSocketAddress address, ChannelHandler handler) throws Exception {
    if (isConnected()) {
      if (address.equals(this.address)) {
        logger.warn("Already connected to {}", address);
        return;
      }

      close();
    }

    Bootstrap bootstrap = new Bootstrap();

    this.address = address;
    bootstrap.group(group);
    bootstrap.channel(NioSocketChannel.class);
    bootstrap.option(ChannelOption.TCP_NODELAY, true);
    bootstrap.option(ChannelOption.SO_RCVBUF, 128 * 1024);
    bootstrap.option(ChannelOption.SO_SNDBUF, 128 * 1024);
    bootstrap.handler(handler);
    channel = bootstrap.connect(address).sync().await().channel();
    if (channel != null && channel.isActive()) {
      logger.info("local {} connected to {}", channel.localAddress(), channel.remoteAddress());
    } else {
      logger.info("fail to connect {}", address);
    }
  }

  public boolean sendMessage(Message msg) {
    if (noConnected()) {
      return false;
    }
    channel.writeAndFlush(msg);
    return true;
  }

  public boolean isConnected() {
    return channel != null && channel.isActive();
  }

  public boolean noConnected() {
    return !isConnected();
  }

  public EventLoopGroup getGroup() {
    return group;
  }

  public Channel getChannel() {
    return channel;
  }

  @Override
  public void close() {
    if (channel == null) {
      return;
    }
    channel.close();
    channel = null;
  }
}
