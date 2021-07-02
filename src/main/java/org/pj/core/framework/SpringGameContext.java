package org.pj.core.framework;

import io.netty.channel.ChannelHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import org.pj.core.ShutdownHook;
import org.pj.core.anno.Facade;
import org.pj.core.framework.disruptor.DisruptorThreadPool;
import org.pj.core.msg.MessageDispatcher;
import org.pj.core.net.NettyTcpServer;
import org.pj.core.net.ThreadCommon;
import org.pj.core.net.handler.MessageHandler;
import org.pj.core.net.init.ProtobufSocketHandlerInitializer;
import org.pj.core.net.init.WebSocketServerHandlerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

public class SpringGameContext implements AutoCloseable, BeanFactory {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private MessageDispatcher dispatcher;
  private NettyTcpServer tcpServer;
  private GenericApplicationContext context;
  private DisruptorThreadPool threadPool;
  private ThreadCommon threadCommon;
  private volatile boolean started;

  public SpringGameContext(GenericApplicationContext ctx) {
    context = ctx;
  }

  public GenericApplicationContext getContext() {
    return context;
  }

  public boolean isStarted() {
    return started;
  }

  public MessageDispatcher getDispatcher() {
    return dispatcher;
  }

  public NettyTcpServer getTcpServer() {
    return tcpServer;
  }

  public DisruptorThreadPool getThreadPool() {
    return threadPool;
  }

  public void setContext(GenericApplicationContext context) {
    this.context = context;
  }

  public void setTcpServer(NettyTcpServer tcpServer) {
    this.tcpServer = tcpServer;
  }

  public void init() {
    threadPool = new DisruptorThreadPool();
    dispatcher = new MessageDispatcher(threadPool);
    threadCommon = new ThreadCommon();
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
      logger.info("{} created", sys.getClass().getSimpleName());
    }

    //注册门面系统
    Map<String, Object> facades = context.getBeansWithAnnotation(Facade.class);
    for (Object obj : facades.values()) {
      dispatcher.registerHandler(obj);
      logger.info("{} created", obj.getClass().getSimpleName());
    }
  }

  private int getPriority(ISystem obj) {
    Class<?> clazz = obj.getClass();
    Priority pri = clazz.getAnnotation(Priority.class);
    if (pri == null) {
      return 5;
    } else if (pri.value() < 1) {
      return 1;
    } else {
      return Math.min(pri.value(), 10);
    }
  }

  public synchronized void start() throws Exception {
    if (started) {
      return;
    }
    started = true;

    init();
    startTcpServer();

    logger.info("Game started type:{}", getProperty("game.module"));
  }

  public void registerShutdownHook() {
    Thread shutdownHook = new ShutdownHook(this);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }

  private void startTcpServer() throws Exception {
    List<ChannelHandler> channelHandlers = new ArrayList<>();
    channelHandlers.add(new MessageHandler(dispatcher));

    boolean isSocket = context.getEnvironment().getProperty("game.isSocket", Boolean.class, false);
    ChannelHandler handler;
    if (isSocket) {
      handler = new ProtobufSocketHandlerInitializer(channelHandlers);
    } else {
      handler = new WebSocketServerHandlerInitializer(channelHandlers);
    }
    tcpServer.startUp(handler, threadCommon);
  }

  public String getProperty(String key) {
    return context.getEnvironment().getProperty(key);
  }

  public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
    return context.getEnvironment().getProperty(key, targetType, defaultValue);
  }

  @Override
  public synchronized void close() throws Exception {
    doClose();
  }

  private void doClose() throws Exception {
    if (!started) {
      return;
    }

    logger.info("shutdown All connections");
    logger.info("shutdown dispatcher");
    dispatcher.close();
    logger.info("shutdown systems");
    destroySystems();
    logger.info("shutdown tcpServer");
    tcpServer.close();
    threadCommon.close();
    started = false;
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

  public Environment getEnvironment() {
    return context.getEnvironment();
  }

}
