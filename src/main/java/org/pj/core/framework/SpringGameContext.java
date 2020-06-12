package org.pj.core.framework;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Priority;
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

  private GenericApplicationContext context;

  public void setContext(GenericApplicationContext context) {
    this.context = context;
  }

  public void start() {
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

    //TODO 触发全部系统初始完毕事件
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

  @Override
  public void close() throws Exception {

    //TODO 断开所有链接
    //TODO 关闭分发器
    destroySystems();
    //TODO 关闭TCP服务器

    context.close();
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
}
