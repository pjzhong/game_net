package org.pj.core.msg;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.msg.MessageProto.Message.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理调用者
 *
 * @author ZJP
 * @since 2020年06月11日 10:03:47
 **/
public class InvokeContext implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(InvokeContext.class);

  public static Recycler<InvokeContext> FACTORY = new Recycler<InvokeContext>() {
    @Override
    protected InvokeContext newObject(Handle<InvokeContext> handle) {
      return new InvokeContext(handle);
    }
  };

  private Channel channel;
  private Message message;
  private transient Handle<InvokeContext> recycleHandle;
  private transient HandlerInfo info;
  private transient long start;

  public InvokeContext(Handle<InvokeContext> handle) {
    this.recycleHandle = handle;
  }

  public void setValue(Channel channel, Message message, HandlerInfo info) {
    this.channel = channel;
    this.message = message;
    this.info = info;
    this.start = System.currentTimeMillis();
  }

  @Override
  public void run() {
    Channel channel = this.channel;
    Message request = this.message;
    try {
      Message message = doRun(this);
      channel.write(message);

      long cost = System.currentTimeMillis() - start;
      if (100 < cost) {
        logger.info("cid [{}] handle message [{}] cost [{}ms]", channel.id(), request.getModule(),
            cost);
      }
    } catch (Exception e) {
      channel.write(sysErr(request));
      logger
          .error(String.format("cid [%s] handle [%s] error", channel.id(), request.getModule()),
              e);
    } finally {
      channel.eventLoop().execute(channel::flush);
      Arrays.fill(info.paramArray(), null);
      recycle();
    }
  }

  private Message sysErr(Message request) {
    Message.Builder builder = Message.newBuilder();
    fillState(builder, request);
    return builder
        .setStat(100)//TODO 规范化 消息状态码
        .build();
  }

  private Message doRun(InvokeContext context) throws Exception {
    List<IAdapter<?>> adapters = context.getInfo().getAdapters();
    Object[] params = context.getInfo().paramArray();

    for (int i = 0, size = params.length; i < size; i++) {
      params[i] = adapters.get(i).adapter(this, info, i);
    }

    Method method = context.getInfo().getMethod();
    Object handler = context.getInfo().getHandler();
    Message request = context.getMessage();

    Object result = method.invoke(handler, params);

    Builder builder = Message.newBuilder();
    if (result instanceof Message) {
      builder.mergeFrom((Message) result);
    } else if (result instanceof MessageLite) {
      builder
          .mergeFrom(context.getMessage()) //TODO 这个Merge很疑惑
          .setBody(((MessageLite) result).toByteString());
    } else {
      logger.error("module {} can't return type {}", request.getModule(),
          result.getClass().getName());
    }

    fillState(builder, request);
    return builder.build();
  }

  private Builder fillState(Message.Builder builder, Message request) {
    int responseType = request.getModule();
    if (0 < responseType) {
      responseType = -responseType;
    }

    builder
        .setSerial(request.getSerial())
        .setModule(responseType);
    if (builder.getStat() == 0) {
      builder.setStat(200);
    }
    return builder;
  }


  public Channel getChannel() {
    return channel;
  }

  public InvokeContext setChannel(Channel channel) {
    this.channel = channel;
    return this;
  }

  public Message getMessage() {
    return message;
  }

  public InvokeContext setMessage(Message message) {
    this.message = message;
    return this;
  }

  public HandlerInfo getInfo() {
    return info;
  }

  public InvokeContext setInfo(HandlerInfo info) {
    this.info = info;
    return this;
  }

  public long getStart() {
    return start;
  }

  public InvokeContext setStart(long start) {
    this.start = start;
    return this;
  }

  public void recycle() {
    if (recycleHandle != null) {
      setValue(null, null, null);
      recycleHandle.recycle(this);
    }
  }
}
