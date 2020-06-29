package org.pj.core.framework;

import org.apache.logging.log4j.LogManager;
import org.springframework.context.support.GenericApplicationContext;

public class ShutdownHook extends Thread {

  private GenericApplicationContext context;

  public ShutdownHook(GenericApplicationContext context) {
    this.context = context;
  }

  @Override
  public void run() {
    if (context == null) {
      return;
    }

    SpringGameContext gameContext = context.getBean(SpringGameContext.class);
    if (gameContext != null) {
      gameContext.doClose();
    }

    context.stop();

    shutdownLog4j2();
  }

  private void shutdownLog4j2() {
    try {
      //shutdown log4j2
      LogManager.shutdown();
    } catch (Exception ignore) {
    }
  }

}
