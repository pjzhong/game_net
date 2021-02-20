package org.pj.core.msg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ObjectUtils;
import org.pj.common.NamedThreadFactory;
import org.pj.core.framework.disruptor.DisruptorThreadPool;
import org.pj.core.msg.HandlerInfo.ParameterInfo;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.msg.adp.ContextAdapter;
import org.pj.core.msg.adp.ProtobufAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDispatcher implements AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Map<Integer, HandlerInfo> handlers;
  private final AtomicInteger msgCount;
  private DisruptorThreadPool disruptorPool;
  private volatile boolean work;

  public MessageDispatcher(int threadSize) {
    disruptorPool = new DisruptorThreadPool(threadSize, 4096,
        new NamedThreadFactory("game-disruptor-thread"));
    msgCount = new AtomicInteger();
    handlers = new ConcurrentHashMap<>();
    work = true;
  }

  public Map<Integer, HandlerInfo> getHandlers() {
    return handlers;
  }


  public void channelActive(ChannelHandlerContext ctx) {
    disruptorPool.bind(ctx.channel());
  }

  public void channelInactive(ChannelHandlerContext ctx){
  }

  public boolean add(Channel channel, Message msg) {
    if (!work || msg == null || channel == null) {
      return false;
    }

    int count = msgCount.get();
    if (count > 100) {
    }

    HandlerInfo handler = handlers.get(msg.getModule());
    if (handler == null) {
      logger.error("No handler for module-{}", msg.getModule());
      channel.write(NoModuleResponse(msg));
      channel.eventLoop().execute(channel::flush);
      return false;
    }

    // TODO 用了，但是效果不明显
    InvokeContext invoker = InvokeContext.FACTORY.get();
    invoker.setValue(channel, msg, handler);
    disruptorPool.exec(channel, invoker);
    return true;
  }

  private Message NoModuleResponse(Message message) {
    return Message.newBuilder()
        .setSerial(message.getSerial())
        .setModule(message.getModule() < 0 ? message.getModule() : -message.getModule())
        .setStat(-1)//TODO 规范错误码
        .build();
  }

  public void registerHandler(Object handler) {
    Class<?> clazz = handler.getClass();
    Method[] methods = clazz.getMethods();

    for (Method m : methods) {
      Packet packet = m.getAnnotation(Packet.class);
      if (packet != null) {
        HandlerInfo info = new HandlerInfo(handler, m);
        parseParameter(m, info);

        boolean suc = handlers.putIfAbsent(packet.value(), info) == null;
        if (!suc) {
          throw new IllegalArgumentException(String
              .format("duplicated handler register %s.%s#%s", clazz.getName(), m.getName(),
                  packet.value()));
        }
      }
    }
  }

  private void parseParameter(Method method, HandlerInfo info) {
    Parameter[] parameters = method.getParameters();
    if (ObjectUtils.isEmpty(parameters)) {
      info.setAdapters(Collections.emptyList());
      info.setParameterInfos(Collections.emptyList());
      return;
    }

    ParameterInfo[] pInfos = new ParameterInfo[parameters.length];
    IAdapter<?>[] adapters = new IAdapter[parameters.length];

    ContextAdapter contextFieldAdapter = ContextAdapter.getInstance();
    ProtobufAdapter protobufParserAdapter = ProtobufAdapter.getInstance();

    for (int i = 0; i < parameters.length; i++) {
      Parameter p = parameters[i];
      pInfos[i] = new ParameterInfo(parameters[i]);
      if (contextFieldAdapter.isContextField(p.getType())) {
        adapters[i] = contextFieldAdapter;
      } else if (protobufParserAdapter.extractParser(p.getType()) != null) {
        adapters[i] = protobufParserAdapter;
      } else {
        throw new IllegalArgumentException(String
            .format("Unresolved able method parameters %s%s#%s",
                method.getDeclaringClass().getName(), method.getName(), p.getName()));
      }
    }

    info.setParameterInfos(Arrays.asList(pInfos));
    info.setAdapters(Arrays.asList(adapters));
  }

  @Override
  public void close() {
    try {
      work = false;
      while (0 < msgCount.get()) {
        logger.info("stopping dispatcher, msgCount:{}", msgCount.get());
        TimeUnit.SECONDS.sleep(1);
      }

      disruptorPool.shutdown();
    } catch (Exception e) {
      logger.info("stooping dispatcher error", e);
    }
  }
}
