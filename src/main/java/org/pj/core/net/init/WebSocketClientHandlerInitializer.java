package org.pj.core.net.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.flush.FlushConsolidationHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.pj.core.net.handler.WebSocketDecoder;
import org.pj.core.net.handler.WebSocketEncoder;

public class WebSocketClientHandlerInitializer extends ChannelInitializer<Channel> {

  private URI uri;
  private List<ChannelHandler> handlers;
  private WebSocketDecoder webSocketDecoder;
  private WebSocketEncoder webSocketEncoder;

  public WebSocketClientHandlerInitializer(URI uri, ChannelHandler handler) {
    this.uri = uri;
    this.handlers = new ArrayList<>();
    handlers.add(handler);
    this.webSocketDecoder = new WebSocketDecoder();
    this.webSocketEncoder = new WebSocketEncoder();
  }

  public WebSocketClientHandlerInitializer(URI uri, List<ChannelHandler> handlers) {
    this.uri = uri;
    this.handlers = new ArrayList<>(handlers);
    this.webSocketDecoder = new WebSocketDecoder();
    this.webSocketEncoder = new WebSocketEncoder();
  }

  public WebSocketClientHandlerInitializer addHandler(ChannelHandler handler) {
    handlers.add(handler);
    return this;
  }

  @Override
  protected void initChannel(Channel ch) {
    ChannelPipeline pip = ch.pipeline();

    pip.addLast(new FlushConsolidationHandler());
    pip.addLast(new HttpClientCodec());
    pip.addLast(new HttpObjectAggregator(Short.MAX_VALUE));
    pip.addLast(new WebSocketClientProtocolHandler(uri, WebSocketVersion.V13, "", true,
        new DefaultHttpHeaders(), Short.MAX_VALUE));
    pip.addLast(webSocketDecoder);
    pip.addLast(webSocketEncoder);
    for (ChannelHandler handler : handlers) {
      pip.addLast(handler);
    }
  }
}
