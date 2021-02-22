package org.pj.core.net;

import com.google.protobuf.MessageLite;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.nio.charset.StandardCharsets;
import org.pj.common.NamedThreadFactory;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class ExampleTcpClient {

  private final String host;
  private final int port;
  private Bootstrap bootstrap;
  private Channel channel;
  private ChannelHandler handler;

  public ExampleTcpClient(String host, int port, ChannelHandler handler)
      throws InterruptedException {
    this.host = host;
    this.port = port;
    this.handler = handler;
    initBootstrap(new NioEventLoopGroup(1, new NamedThreadFactory("tcp_client")));
  }

  public ExampleTcpClient(String host, int port, EventLoopGroup group, ChannelHandler handler)
      throws InterruptedException {
    this.host = host;
    this.port = port;
    this.handler = handler;
    initBootstrap(group);
  }

  public void sendMsg(String msg) {
    ByteBuf buf = channel.alloc().buffer();
    buf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
    channel.writeAndFlush(buf, channel.voidPromise());
  }

  public void sendMsg(MessageLite packet) {
    ByteBuf buf = channel.alloc().buffer();
    buf.writeBytes(packet.toByteArray());
    channel.writeAndFlush(buf, channel.voidPromise());
  }

  public void sendMsg(byte[] packet) {
    ByteBuf buf = channel.alloc().buffer();
    buf.writeBytes(packet);
    channel.writeAndFlush(buf);
  }

  private void initBootstrap(EventLoopGroup loopGroup) throws InterruptedException {
    bootstrap = new Bootstrap()
        .group(loopGroup)
        .channel(NioSocketChannel.class)
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        .option(ChannelOption.SO_KEEPALIVE, false)
        // 设置禁用nagle算法
        .option(ChannelOption.TCP_NODELAY, true)
        // 连接超时
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200)
        .handler(handler);

    channel = bootstrap.connect(host, port).sync().channel();
  }

  public void close() {
    channel.close();
    bootstrap.config().group().shutdownGracefully();
  }

}
