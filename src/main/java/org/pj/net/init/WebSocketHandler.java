package org.pj.net.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import org.pj.net.handler.WebSocketDecoder;
import org.pj.net.handler.WebSocketEncoder;

public class WebSocketHandler extends ChannelInitializer {

  private ChannelHandler handler;
  private WebSocketDecoder webSocketDecoder;
  private WebSocketEncoder webSocketEncoder;

  public WebSocketHandler(ChannelHandler handler) {
    this.handler = handler;
    this.webSocketDecoder = new WebSocketDecoder();
    this.webSocketEncoder = new WebSocketEncoder();
  }

  @Override
  protected void initChannel(Channel ch) {
    ChannelPipeline pip = ch.pipeline();

    pip.addLast(new FlushConsolidationHandler());
    pip.addLast(new HttpServerCodec());
    pip.addLast(new HttpObjectAggregator(65536));
    pip.addLast(new WebSocketServerCompressionHandler());
    pip.addLast(new WebSocketServerProtocolHandler("/"));
    pip.addLast(webSocketDecoder);
    pip.addLast(webSocketEncoder);
    //TODO My Message Decoder
    pip.addLast(handler);//TODO MY Message Handler

  }
}
