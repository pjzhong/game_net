package org.pj.core.framework.disruptor;

public class GameRunnable implements Runnable {

  /** 名字 */
  private String name;
  /** 任务 */
  private Runnable runnable;

  public GameRunnable() {}

  @Override
  public void run() {
    runnable.run();
  }

  public String getName() {
    return name;
  }

  public GameRunnable setName(String name) {
    this.name = name;
    return this;
  }

  public Runnable getRunnable() {
    return runnable;
  }

  public GameRunnable setRunnable(Runnable runnable) {
    this.runnable = runnable;
    return this;
  }
}
