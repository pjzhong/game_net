package org.pj.msg;

import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ObjectUtils;
import org.pj.common.NamedThreadFactory;
import org.pj.msg.HandlerInfo.ParameterInfo;
import org.pj.msg.MessageProto.Message;
import org.pj.msg.adp.ContextAdapter;
import org.pj.msg.adp.ProtobufAdapter;
import org.pj.thread.GameThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDispatcher implements AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Map<Integer, HandlerInfo> handlers;
  private final AtomicInteger msgCount;
  private final GameThreadPool pool;
  private volatile boolean work;

  public MessageDispatcher(int threadSize) {
    pool = new GameThreadPool(threadSize, new NamedThreadFactory("GameThread"));
    msgCount = new AtomicInteger();
    handlers = new ConcurrentHashMap<>();
    work = true;
  }

  public Map<Integer, HandlerInfo> getHandlers() {
    return handlers;
  }

  public boolean add(Channel channel, Message msg) {
    if (!work || msg == null || channel == null) {
      return false;
    }

    int count = msgCount.get();
    if (count > 100) {
      printState(pool);
    }

    HandlerInfo handler = handlers.get(msg.getModule());
    if (handler == null) {
      logger.error("No handler from {}", msg.getModule());
      return false;
    }

    Executor executor = pool.getPool(channel);
    MessageInvoker invoker = new MessageInvoker(new InvokeContext(channel, msg), handler, msgCount);
    executor.execute(invoker);
    msgCount.incrementAndGet();
    return true;
  }

  public void registerHandler(Object handler) {
    Class<?> clazz = handler.getClass();
    Method[] methods = clazz.getMethods();

    for (Method m : methods) {
      Packet packet = m.getAnnotation(Packet.class);
      if (packet != null) {
        HandlerInfo info = new HandlerInfo(handler, m);
        parseParameter(m, info);

        handlers.put(packet.value(), info);
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

    List<ParameterInfo> pInfos = new ArrayList<>(parameters.length);
    List<IAdapter<?>> adapters = new ArrayList<>(parameters.length);

    ContextAdapter contextFieldAdapter = ContextAdapter.getInstance();
    ProtobufAdapter protobufParserAdapter = ProtobufAdapter.getInstance();

    for (Parameter p : parameters) {
      pInfos.add(new ParameterInfo(p));
      if (contextFieldAdapter.isContextField(p.getType())) {
        adapters.add(contextFieldAdapter);
      } else if (protobufParserAdapter.extractParser(p.getType()) != null) {
        adapters.add(protobufParserAdapter);
      } else {
        throw new IllegalArgumentException(String
            .format("Unresolved able method parameters %s%s#%s",
                method.getDeclaringClass().getName(), method.getName(), p.getName()));
      }
    }
    info.setParameterInfos(pInfos);
    info.setAdapters(adapters);
  }

  @Override
  public void close() {
    try {
      work = false;
      while (0 < msgCount.get()) {
        logger.info("stopping dispatcher, msgCount:{}", msgCount.get());
        TimeUnit.SECONDS.sleep(1);
      }

      pool.shutdown();
    } catch (Exception e) {
      logger.info("stooping dispatcher error", e);
    }
  }

  private void printState(GameThreadPool pool) {
    Map<Integer, Long> stats = pool.getHashStat();
    Collection<Long> statList = stats.values();
    logger.warn("msgQueue size: {}", msgCount.get());
    if (0 < statList.size()) {
      logger.warn("min:{}, max:{}", Collections.min(statList), Collections.max(statList));
    }
    logger.warn("{}", stats);
  }
}
