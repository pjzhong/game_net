package org.pj.core.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import org.pj.core.msg.MessageProto.Message;

/**
 * 默认Message编码和解码器
 *
 * @author ZJP
 * @since 2020年06月11日 11:18:52
 **/
@Sharable
public class ProtoBufCodec extends MessageToMessageCodec<ByteBuf, Message> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
    byte[] bytes = msg.toByteArray();
    ByteBuf buf = ctx.alloc().buffer(bytes.length);
    buf.writeBytes(bytes);
    out.add(buf);
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    out.add(Message.parseFrom(msg.nioBuffer()));
  }
}
