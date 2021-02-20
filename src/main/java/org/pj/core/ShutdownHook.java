package org.pj.core;

import org.apache.logging.log4j.LogManager;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.net.ThreadCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

public class ShutdownHook extends Thread {

  private Logger shutDownLogger = LoggerFactory.getLogger(this.getClass());

  private GenericApplicationContext context;

  public ShutdownHook(GenericApplicationContext context) {
    this.context = context;
  }

  @Override
  public void run() {
    if (context == null || !context.isRunning()) {
      return;
    }

    SpringGameContext gameContext = context.getBean(SpringGameContext.class);
    if (gameContext != null) {
      try {
        gameContext.doClose();
      } catch (Exception e) {
        shutDownLogger.error("Close GameContext exception", e);
      }
    }

    context.stop();

    ThreadCommon.WORKER.shutdownGracefully();
    ThreadCommon.BOSS.shutdownGracefully();

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
