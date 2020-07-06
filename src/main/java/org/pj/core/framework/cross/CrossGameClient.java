package org.pj.core.framework.cross;

import static io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.NettyTcpClient;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketClientHandlerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossGameClient extends SimpleChannelInboundHandler<Message> {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  private Map<Integer, SocketCallback<?>> callbacks;
  private SpringGameContext context;
  private NettyTcpClient client;
  private AtomicInteger msgId;
  private CountDownLatch webSocketWait;
  private CrossSendProxy proxy;

  public CrossGameClient(SpringGameContext ctx) {
    this.context = ctx;
    this.callbacks = new ConcurrentHashMap<>();
    this.msgId = new AtomicInteger();
    this.client = new NettyTcpClient(ctx.getTcpServer().getBootstrap().config().childGroup());
    this.proxy = new CrossSendProxy(this);
  }

  public void addSocketCallback(int msgId, SocketCallback<?> callBack) {
    this.callbacks.put(msgId, callBack);
  }

  public SocketCallback<?> removeCallBack(int msgId) {
    return callbacks.remove(msgId);
  }

  public <T, R> T asyncProxy(Class<T> clz, ResultCallBack<R> callback) {
    return proxy.asynProxy(clz, callback);
  }

  public <T> T syncProxy(Class<T> clz) {
    return proxy.snycProxy(clz);
  }

  public int genMsgId() {
    int id = msgId.incrementAndGet();
    if (id <= 0) {
      msgId.compareAndSet(id, 0);
      id = msgId.incrementAndGet();
    }
    return id;
  }

  //TODO 提供非阻塞式链接
  public void connect(URI uri) throws Exception {
    InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    List<ChannelHandler> channelHandlers = Collections.singletonList(this);
    boolean isSocket = context.getProperty("game.isSocket", Boolean.class, false);
    ChannelHandler handler;
    if (isSocket) {
      handler = new ProtobufSocketHandlerInitializer(channelHandlers);
    } else {
      webSocketWait = new CountDownLatch(1);
      handler = new WebSocketClientHandlerInitializer(uri, channelHandlers);
    }
    client.connect(address, handler);
    if (webSocketWait != null) {
      webSocketWait.await();
    }
  }

  public boolean sendMessage(Message msg) {
    return client.sendMessage(msg);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (webSocketWait != null && evt == HANDSHAKE_COMPLETE) {
      webSocketWait.countDown();
    }
    ctx.fireUserEventTriggered(evt);
  }


  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    context.getChannels().add(ctx.channel());
    ctx.fireChannelActive();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    if (webSocketWait != null) {
      webSocketWait.countDown();
    }
    ctx.fireChannelInactive();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
    SocketCallback<Object> callback = (SocketCallback<Object>) callbacks.remove(msg.getSerial());
    if (callback != null) { //TODO 执行回调
      ExecutorService group = context.getExecutorService();
      group.execute(() -> callback.accept(msg));
    } else {
      //内部消息
      context.getDispatcher().add(ctx.channel(), msg);
    }
  }
}
