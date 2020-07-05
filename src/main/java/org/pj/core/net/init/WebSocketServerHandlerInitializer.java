package org.pj.core.net.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import java.util.ArrayList;
import java.util.List;
import org.pj.core.net.handler.WebSocketDecoder;
import org.pj.core.net.handler.WebSocketEncoder;

public class WebSocketServerHandlerInitializer extends ChannelInitializer<Channel> {

  private List<ChannelHandler> handlers;
  private WebSocketDecoder webSocketDecoder;
  private WebSocketEncoder webSocketEncoder;

  public WebSocketServerHandlerInitializer(ChannelHandler handler) {
    this.handlers = new ArrayList<>();
    handlers.add(handler);
    this.webSocketDecoder = new WebSocketDecoder();
    this.webSocketEncoder = new WebSocketEncoder();
  }

  public WebSocketServerHandlerInitializer(List<ChannelHandler> handlers) {
    this.handlers = new ArrayList<>(handlers);
    this.webSocketDecoder = new WebSocketDecoder();
    this.webSocketEncoder = new WebSocketEncoder();
  }

  public WebSocketServerHandlerInitializer addHandler(ChannelHandler handler) {
    handlers.add(handler);
    return this;
  }

  @Override
  protected void initChannel(Channel ch) {
    ChannelPipeline pip = ch.pipeline();

    pip.addLast(new FlushConsolidationHandler());
    pip.addLast(new HttpServerCodec());
    pip.addLast(new HttpObjectAggregator(Short.MAX_VALUE));
    pip.addLast(new WebSocketServerCompressionHandler());
    pip.addLast(new WebSocketServerProtocolHandler("/"));
    pip.addLast(webSocketDecoder);
    pip.addLast(webSocketEncoder);
    for (ChannelHandler handler : handlers) {
      pip.addLast(handler);
    }
  }
}
