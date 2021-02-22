package org.pj.core.net.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.util.List;
import org.pj.core.msg.Message;

/**
 * SimpleDecoder
 *
 * @author ZJP
 * @since 2020年04月02日 18:09:00
 **/
@Sharable
public class WebSocketDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {


  @Override
  protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {
    out.add(Message.readFrom(msg.content()));
  }
}
