package org.pj.core;

import org.pj.core.framework.SpringGameContext;

public class ShutdownHook extends Thread {

  private SpringGameContext context;

  public ShutdownHook(SpringGameContext context) {
    this.context = context;
  }

  @Override
  public void run() {
    if (context == null) {
      return;
    }

    try {
      context.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
