package org.pj.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.util.List;

/**
 * SimpleDecoder
 *
 * @author ZJP
 * @since 2020年04月02日 18:09:00
 **/
public class WebSocketDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {


  @Override
  protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {
    out.add(msg.content().retain());
  }
}
