package org.pj.core.net.handler;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.util.List;

/**
 * Simple Encoder
 *
 * @author ZJP
 * @since 2020年04月02日 18:08:49
 **/
@Sharable
public class WebSocketEncoder extends MessageToMessageEncoder<MessageLite> {

  @Override
  protected void encode(ChannelHandlerContext ctx, MessageLite msg, List<Object> out) {
    byte[] bytes = msg.toByteArray();
    ByteBuf buf = ctx.alloc().buffer(bytes.length);
    buf.writeBytes(bytes);
    out.add(new BinaryWebSocketFrame(buf));
  }
}