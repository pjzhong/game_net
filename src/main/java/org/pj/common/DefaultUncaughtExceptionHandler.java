package org.pj.common;

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认线程异常记录
 *
 * @author ZJP
 * @since 2020年04月13日 20:50:49
 **/
public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {

  private static final Logger logger = LoggerFactory
      .getLogger(DefaultUncaughtExceptionHandler.class);

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    logger.error("uncaughtException from thread", e);
  }
}
