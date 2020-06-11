package org.pj.msg;

import com.google.protobuf.MessageLite;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.pj.msg.MessageProto.Message;

/**
 * 消息处理调用者
 *
 * @author ZJP
 * @since 2020年06月11日 10:03:47
 **/
public class MessageInvoker implements Runnable {

  private final InvokeContext context;
  private final HandlerInfo info;
  private final AtomicInteger msgCount;

  public MessageInvoker(InvokeContext context, HandlerInfo info,
      AtomicInteger msgCount) {
    this.context = context;
    this.info = info;
    this.msgCount = msgCount;
  }

  @Override
  public void run() {
    try {
      List<IAdapter<?>> adapters = info.getAdapters();
      Object[] params =
          adapters.isEmpty() ? ArrayUtils.EMPTY_OBJECT_ARRAY : new Object[adapters.size()];

      for (int i = 0, size = params.length; i < size; i++) {
        params[i] = adapters.get(i).adapter(context, info, i);
      }

      Method method = info.getMethod();
      Object handler = info.getHandler();

      Object result = method.invoke(handler, params);

      if (result != null) {
        if (result instanceof Message) {
          context.getChannel().write(result);
        } else if (result instanceof MessageLite) {
          Message response = Message
              .newBuilder()
              .mergeFrom(context.getMessage())
              .setBody(((MessageLite) result).toByteString()).build();
          context.getChannel().write(response);
        }

        context.getChannel().flush();
      }
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Handler %s error", context.getMessage().getModule()), e);
    } finally {
      msgCount.decrementAndGet();
    }
  }
}
