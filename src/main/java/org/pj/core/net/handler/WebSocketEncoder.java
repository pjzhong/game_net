package org.pj.core.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.util.List;
import org.pj.core.msg.Message;

/**
 * Simple Encoder
 *
 * @author ZJP
 * @since 2020年04月02日 18:08:49
 **/
@Sharable
public class WebSocketEncoder extends MessageToMessageEncoder<Message> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) {
    ByteBuf buf = ctx.alloc().buffer();
    msg.toByteArray(buf);
    out.add(new BinaryWebSocketFrame(buf));
  }
}
