package org.pj.core.net.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.flush.FlushConsolidationHandler;
import java.util.ArrayList;
import java.util.List;
import org.pj.core.net.handler.ProtoBufCodec;

public class ProtobufSocketHandlerInitializer extends ChannelInitializer {

  private LengthFieldPrepender lengthFieldPrepender;
  private ProtoBufCodec protoBufCodec;
  private List<ChannelHandler> handlers;

  public ProtobufSocketHandlerInitializer(ChannelHandler handler) {
    this.handlers = new ArrayList<>();
    this.handlers.add(handler);
    this.lengthFieldPrepender = new LengthFieldPrepender(Short.BYTES);
    this.protoBufCodec = new ProtoBufCodec();
  }

  public ProtobufSocketHandlerInitializer addHandler(ChannelHandler handler) {
    handlers.add(handler);
    return this;
  }

  @Override
  protected void initChannel(Channel ch) {
    ChannelPipeline pip = ch.pipeline();
    pip.addLast(new FlushConsolidationHandler());
    pip.addLast(lengthFieldPrepender);
    pip.addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, Short.BYTES, 0,
        Short.BYTES));
    pip.addLast(protoBufCodec);
    for (ChannelHandler handler : handlers) {
      pip.addLast(handler);
    }
  }
}
