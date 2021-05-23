package org.pj.core.framework.disruptor;

/**
 * 给Disruptor的事件类
 *
 * @author ZJP
 * @since 2021年05月16日 18:18:38
 **/
public final class RunnableWrapper implements Runnable {

  /** 任务名字 */
  private String name;
  /** 任务 */
  private Runnable runnable;

  @Override
  public void run() {
    try {
      if (runnable != null) {
        runnable.run();
      }
    } finally {
      runnable = null;
    }
  }

  public Runnable getRunnable() {
    return runnable;
  }

  public RunnableWrapper setRunnable(Runnable runnable) {
    this.runnable = runnable;
    return this;
  }

  public String getName() {
    return name;
  }

  public RunnableWrapper setName(String name) {
    this.name = name;
    return this;
  }
}
