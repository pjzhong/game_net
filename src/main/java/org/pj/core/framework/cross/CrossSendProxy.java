package org.pj.core.framework.cross;

import com.google.protobuf.MessageLite;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.pj.core.msg.Message;
import org.pj.core.msg.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossSendProxy {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private Map<Class<?>, Object> ivkCaches;
  private InvocationHandler invoker;
  private ThreadLocal<ResultCallBack<?>> currentCallBack = new ThreadLocal<>();

  public CrossSendProxy(CrossGameClient client) {
    this.invoker = new SendProxyInvoker(client);
    this.ivkCaches = new ConcurrentHashMap<>();
  }

  public <T, R> T asynProxy(Class<T> clz, ResultCallBack<R> callback) {
    T t = createProxy(clz);
    currentCallBack.set(callback);
    return t;
  }

  public <T> T snycProxy(Class<T> clz) {
    return createProxy(clz);
  }

  private <T> T createProxy(Class<T> clz) {
    return (T) ivkCaches.computeIfAbsent(clz,
        k -> Proxy.newProxyInstance(k.getClassLoader(), new Class[]{k}, this.invoker));
  }

  private class SendProxyInvoker implements InvocationHandler {

    private CrossGameClient client;

    public SendProxyInvoker(CrossGameClient client) {
      this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Exception {

      if (method.getDeclaringClass() == Object.class) {
        return method.invoke(this, args);
      }

      Packet packet = method.getAnnotation(Packet.class);
      if (packet == null) {
        logger.error("{}.{} don't have packet annotation", method.getDeclaringClass().getName(),
            method.getName());
        return null;
      }
      Message builder = Message.valueOf()
          .setModule(packet.value())
          .setOpt(client.genMsgId());
      if (args != null) {
        for (Object o : args) {
          if (o instanceof MessageLite) {
            MessageLite lite = (MessageLite) o;
            builder.setBody(lite.toByteArray());
            break;
          }
        }
      }

      Message message = Message.valueOf();
      ResultCallBack<?> callback = currentCallBack.get();
      SocketCallback<?> clientCallBack = null;
      CompletableFuture<?> future = null;
      currentCallBack.remove();
      if (callback == null) {// TODO 阻塞调用
        future = new CompletableFuture<>();
        clientCallBack = new ProtoBufCallBackAdapter(method, new CompleteSocketCallBack(future));
      } else {
        clientCallBack = new ProtoBufCallBackAdapter(method, callback);
      }

      client.addSocketCallback(message.getOpt(), clientCallBack);
      boolean suc = client.sendMessage(message);
      if (!suc) {
        client.removeCallBack(message.getOpt());
      }

      return future != null ?  future.get(10, TimeUnit.SECONDS) : null;
    }
  }

}
