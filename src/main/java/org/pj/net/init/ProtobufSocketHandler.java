package org.pj.net.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.flush.FlushConsolidationHandler;
import org.pj.net.handler.ProtoBufCodec;

public class ProtobufSocketHandler extends ChannelInitializer {

  private ChannelHandler handler;
  private LengthFieldPrepender lengthFieldPrepender;
  private ProtoBufCodec protoBufCodec;

  public ProtobufSocketHandler(ChannelHandler handler) {
    this.handler = handler;
    this.lengthFieldPrepender = new LengthFieldPrepender(Short.BYTES);
    this.protoBufCodec = new ProtoBufCodec();
  }

  @Override
  protected void initChannel(Channel ch) {
    ChannelPipeline pip = ch.pipeline();
    pip.addLast(new FlushConsolidationHandler());
    pip.addLast(lengthFieldPrepender);
    pip.addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, Short.BYTES, 0,
        Short.BYTES));
    pip.addLast(protoBufCodec);
    pip.addLast(handler);
  }
}
