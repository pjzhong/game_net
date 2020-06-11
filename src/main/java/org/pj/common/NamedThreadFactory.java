package org.pj.common;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可命名线程工厂
 *
 * @author kingston
 */
public class NamedThreadFactory implements ThreadFactory {


  /** 是否为守护线程 */
  private final boolean daemo;
  /** 名字 */
  private String groupName;
  /** 累计 */
  private AtomicInteger idGenerator = new AtomicInteger(1);
  /** 未知异常处理 */
  private UncaughtExceptionHandler uncaughtExceptionHandler;

  public NamedThreadFactory(String group) {
    this(group, false, new DefaultUncaughtExceptionHandler());
  }

  public NamedThreadFactory(String group, boolean daemo, UncaughtExceptionHandler handler) {
    this.groupName = group;
    this.daemo = daemo;
    this.uncaughtExceptionHandler = handler;
  }

  @Override
  public Thread newThread(Runnable r) {
    String name = getNextThreadName();
    Thread ret = new Thread(null, r, name, 0);
    ret.setDaemon(daemo);
    ret.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    return ret;
  }

  private String getNextThreadName() {
    return this.groupName + "-" + this.idGenerator.getAndIncrement();
  }

}
