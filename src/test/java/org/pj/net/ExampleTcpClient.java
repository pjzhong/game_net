package org.pj.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.flush.FlushConsolidationHandler;
import java.nio.charset.StandardCharsets;

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
    initBootstrap();
  }

  public void sendMsg(String msg) {
    ByteBuf buf = channel.alloc().buffer();
    buf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
    channel.writeAndFlush(buf);
  }

  private void initBootstrap() throws InterruptedException {
    EventLoopGroup loopGroup = new NioEventLoopGroup(4);
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

  /**
   * @author zhongjp
   * @since 2018/7/6
   */
  public static class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelHandler handler;

    public ChatClientInitializer(ChannelHandler handler) {
      this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();
      int maxLength = 1024 * 1024;
      int lengthField = 4;
      int offset = 0;
      pipeline.addLast(new FlushConsolidationHandler());
      pipeline.addLast(new LengthFieldPrepender(lengthField));
      pipeline.addLast(
          new LengthFieldBasedFrameDecoder(maxLength, offset, lengthField, 0, lengthField));
      pipeline.addLast(handler);
    }
  }


}
