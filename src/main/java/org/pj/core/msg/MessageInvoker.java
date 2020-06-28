package org.pj.core.msg;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.pj.core.msg.MessageProto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理调用者
 *
 * @author ZJP
 * @since 2020年06月11日 10:03:47
 **/
public class MessageInvoker implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(MessageInvoker.class);

  private final InvokeContext context;
  private final HandlerInfo info;
  private final AtomicInteger msgCount;
  private final long start;

  public MessageInvoker(InvokeContext context, HandlerInfo info,
      AtomicInteger msgCount) {
    this.context = context;
    this.info = info;
    this.msgCount = msgCount;
    this.start = System.currentTimeMillis();
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
      Channel channel = context.getChannel();

      //TODO RESPONSE HANDLE
      if (result != null) {
        if (result instanceof Message) {
          channel.write(result);
        } else if (result instanceof MessageLite) {
          Message response = Message
              .newBuilder()
              .mergeFrom(context.getMessage())
              .setBody(((MessageLite) result).toByteString()).build();
          channel.write(response);
        }

      } else {
        Message response = Message
            .newBuilder()
            .build();
        channel.write(response);
      }
      channel.flush();
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Handler %s error", context.getMessage().getModule()), e);
    } finally {
      msgCount.decrementAndGet();
    }

    long cost = System.currentTimeMillis() - start;
    if (100 < cost) {
      logger.info("handle message [{}] cost [{}ms]", context.getMessage().getModule(), cost);
    }
  }
}
