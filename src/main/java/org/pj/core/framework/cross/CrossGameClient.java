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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.net.NettyTcpClient;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketClientHandlerInitializer;

public class CrossGameClient extends SimpleChannelInboundHandler<Message> {


  private Map<Integer, SocketCallback<Object>> callbacks;
  private SpringGameContext context;
  private NettyTcpClient client;
  private AtomicInteger msgId;
  private CountDownLatch connectWait;
  private CrossSendProxy proxy;

  public CrossGameClient(SpringGameContext ctx) {
    this.context = ctx;
    this.callbacks = new ConcurrentHashMap<>();
    this.msgId = new AtomicInteger();
    this.client = new NettyTcpClient(ctx.getTcpServer().getBootstrap().config().childGroup());
    this.proxy = new CrossSendProxy(this);
  }

  public <T> void addSocketCallback(int msgId, SocketCallback<T> callBack) {
    this.callbacks.put(msgId, (SocketCallback<Object>) callBack);
  }

  public SocketCallback<?> removeCallBack(int msgId) {
    return callbacks.remove(msgId);
  }

  public <T, R> T asyncProxy(Class<T> clz, SocketCallback<R> callback) {
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
      connectWait = new CountDownLatch(1);
      handler = new WebSocketClientHandlerInitializer(uri, channelHandlers);
    }
    client.connect(address, handler);
    if (connectWait != null) {
      connectWait.await(10, TimeUnit.SECONDS);
    }
  }

  public boolean sendMessage(Message msg) {
    return client.sendMessage(msg);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (connectWait != null && evt == HANDSHAKE_COMPLETE) {
      connectWait.countDown();
    }
    ctx.fireUserEventTriggered(evt);
  }


  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    context.getDispatcher().channelActive(ctx);
    ctx.fireChannelActive();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    if (connectWait != null) {
      connectWait.countDown();
    }
    ctx.fireChannelInactive();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
    SocketCallback<Object> callback = callbacks.remove(msg.getSerial());
    if (callback != null) {
      context.getThreadPool().exec(ctx.channel(), () -> callback.accept(msg));
    } else {
      //内部消息
      context.getDispatcher().add(ctx.channel(), msg);
    }
  }
}
