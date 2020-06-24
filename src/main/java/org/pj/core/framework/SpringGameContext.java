package org.pj.core.framework;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import org.apache.commons.lang3.ArrayUtils;
import org.pj.core.event.EventBus;
import org.pj.core.msg.MessageDispatcher;
import org.pj.core.net.TcpServer;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketHandlerInitializer;
import org.pj.protocols.Facade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;

public class SpringGameContext implements AutoCloseable, BeanFactory {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private Set<Channel> channels;
  private ExecutorService service;
  private EventBus eventBus;
  private MessageDispatcher dispatcher;
  private TcpServer tcpServer;
  private GenericApplicationContext context;

  public SpringGameContext(GenericApplicationContext ctx) {
    channels = new ConcurrentSkipListSet<>();
    context = ctx;
    service = workStrealingPool();
  }

  private ForkJoinPool workStrealingPool() {
    final ForkJoinWorkerThreadFactory factory = pool -> {
      final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory
          .newThread(pool);
      worker.setName("game-context-" + worker.getPoolIndex());
      return worker;
    };

    return new ForkJoinPool
        (Runtime.getRuntime().availableProcessors(), factory, null, true);
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public void setContext(GenericApplicationContext context) {
    this.context = context;
  }

  public void setDispatcher(MessageDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public void setTcpServer(TcpServer tcpServer) {
    this.tcpServer = tcpServer;
  }

  public void init() {
    initSystem();
  }

  private void initSystem() {
    Map<String, ISystem> systems = context.getBeansOfType(ISystem.class);
    Map<String, Integer> sysPriority = new HashMap<>(systems.size());
    systems.forEach((k, v) -> sysPriority.put(k, getPriority(v)));

    List<ISystem> systemList = systems.entrySet().stream()
        .sorted(Comparator.comparingInt(e -> sysPriority.getOrDefault(e.getKey(), 5)))
        .map(Entry::getValue)
        .collect(Collectors.toList());

    for (ISystem sys : systemList) {
      sys.init();
      eventBus.registerEvent(sys);
      logger.info("{} created", sys.getClass().getSimpleName());
    }

    //注册门面系统
    Map<String, Object> facades = context.getBeansWithAnnotation(Facade.class);
    for (Object obj : facades.values()) {
      dispatcher.registerHandler(obj);
    }

    //触发全部系统初始完毕事件
    fireEvent(SystemEvent.ALL_SYSTEM_INIT.getType());
  }

  private int getPriority(ISystem obj) {
    Class<?> clazz = obj.getClass();
    Priority pri = clazz.getAnnotation(Priority.class);
    if (pri == null) {
      return 5;
    } else if (pri.value() < 1) {
      return 1;
    } else {
      return pri.value() > 10 ? 10 : pri.value();
    }
  }

  public void start() throws Exception {
    startTcpServer();

    Runtime.getRuntime().removeShutdownHook(new Thread(this::close));
  }

  private void startTcpServer() throws Exception {
    List<ChannelHandler> channelHandlers = new ArrayList<>();
    channelHandlers.add(new ChannelCollector(channels));
    channelHandlers.add(new MessageHandler(dispatcher));

    boolean isSocket = context.getEnvironment().getProperty("game.isSocket", Boolean.class, false);
    ChannelHandler handler;
    if (isSocket) {
      handler = new ProtobufSocketHandlerInitializer(channelHandlers);
    } else {
      handler = new WebSocketHandlerInitializer(channelHandlers);
    }
    tcpServer.startUp(handler);
  }

  @Override
  public void close() {
    logger.info("shutdown All connections");
    channels.forEach(Channel::close);
    logger.info("shutdown dispatcher");
    dispatcher.close();
    logger.info("shutdown systems");
    destroySystems();
    logger.info("shutdown tcpServer");
    tcpServer.close();

    logger.info("gameContext shutdown success");
  }

  private void destroySystems() {
    Map<String, ISystem> systems = context.getBeansOfType(ISystem.class);
    for (ISystem sys : systems.values()) {
      try {
        sys.destroy();
      } catch (Exception e) {
        logger.error(sys.getClass().getSimpleName() + " destroy error", e);
      }
    }
  }

  private static class ChannelCollector extends ChannelInboundHandlerAdapter {

    private final Set<Channel> channels;

    public ChannelCollector(Set<Channel> channels) {
      this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      channels.add(ctx.channel());
      ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      channels.remove(ctx.channel());
      ctx.fireChannelInactive();
    }
  }

  /*    事件方法            */
  public void fireEvent(int type) {
    eventBus.fireEvent(type, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  public void fireEvent(int type, Object... params) {
    eventBus.fireEvent(type, params);
  }

  public void asyncFireEvent(int type) {
    eventBus.asyncFireEvent(service, type, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  public void asyncFireEvent(int type, Object... params) {
    eventBus.asyncFireEvent(service, type, params);
  }

  /*    Spring 代理方法            */
  @Override
  public Object getBean(String name) throws BeansException {
    return context.getBean(name);
  }

  @Override
  public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
    return context.getBean(name, requiredType);
  }

  @Override
  public Object getBean(String name, Object... args) throws BeansException {
    return context.getBean(name, args);
  }

  @Override
  public <T> T getBean(Class<T> requiredType) throws BeansException {
    return context.getBean(requiredType);
  }

  @Override
  public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
    return context.getBean(requiredType, args);
  }

  @Override
  public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
    return context.getBeanProvider(requiredType);
  }

  @Override
  public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
    return context.getBeanProvider(requiredType);
  }

  @Override
  public boolean containsBean(String name) {
    return context.containsBean(name);
  }

  @Override
  public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
    return context.isSingleton(name);
  }

  @Override
  public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
    return context.isPrototype(name);
  }

  @Override
  public boolean isTypeMatch(String name, ResolvableType typeToMatch)
      throws NoSuchBeanDefinitionException {
    return context.isTypeMatch(name, typeToMatch);
  }

  @Override
  public boolean isTypeMatch(String name, Class<?> typeToMatch)
      throws NoSuchBeanDefinitionException {
    return context.isTypeMatch(name, typeToMatch);
  }

  @Override
  public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
    return context.getType(name);
  }

  @Override
  public Class<?> getType(String name, boolean allowFactoryBeanInit)
      throws NoSuchBeanDefinitionException {
    return context.getType(name, allowFactoryBeanInit);
  }

  @Override
  public String[] getAliases(String name) {
    return context.getAliases(name);
  }
}
