package org.pj.net.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class SocketHandler extends ChannelInitializer {

  private ChannelHandler handler;
  private LengthFieldPrepender prepender;
  private int maxLength = 1024 * 1024;
  private int offset = 0;
  private int lengthFieldLength = 4;

  public SocketHandler(ChannelHandler handler) {
    this.handler = handler;
    this.prepender = new LengthFieldPrepender(lengthFieldLength);
  }

  @Override
  protected void initChannel(Channel ch) {
    ChannelPipeline pip = ch.pipeline();
    pip.addLast(prepender);
    pip.addLast(new LengthFieldBasedFrameDecoder(maxLength, offset, lengthFieldLength, 0,
        lengthFieldLength));
    pip.addLast(handler);
  }
}
