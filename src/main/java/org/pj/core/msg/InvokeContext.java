package org.pj.core.msg;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
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

  private final transient Handle<InvokeContext> recycleHandle;
  private Channel channel;
  private Message message;
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
        logger.debug("cid [{}] handle message [{}] cost [{}ms]", channel.remoteAddress(),
            request.getModule(),
            cost);
      }
    } catch (Exception e) {
      channel.write(sysErr(request));
      logger
          .error("cid [{}] handle module [{}] error", channel.id(), request.getModule(),
              e);
    } finally {
      channel.eventLoop().execute(channel::flush);
      Arrays.fill(info.paramArray(), null);
      recycle();
    }
  }

  private Message sysErr(Message request) {
    Message builder = Message.valueOf();
    fillState(builder, request);
    return builder
        .setStates(SystemStates.SYSTEM_ERR);
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

    Message response = Message.valueOf();
    if (result instanceof Message) {
      response.setBody(((Message) result).getBody());
    } else if (result instanceof MessageLite) {
      response
          .setBody(((MessageLite) result).toByteArray());
    } else {
      logger.error("module {} can't return type {}", request.getModule(),
          result.getClass().getName());
    }

    fillState(response, request);
    return response;
  }

  private Message fillState(Message builder, Message request) {
    int responseType = request.getModule();
    if (0 < responseType) {
      responseType = -responseType;
    }

    builder
        .setOpt(request.getOpt())
        .setModule(responseType);
    if (builder.getStates() == 0) {
      builder.setStates(SystemStates.OK);
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
