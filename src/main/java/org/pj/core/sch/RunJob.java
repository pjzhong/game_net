package org.pj.core.sch;

import java.util.concurrent.Future;
import java.util.function.Consumer;

class RunJob implements Runnable {

  /** 定时器 */
  private final Trigger trigger;
  private final Consumer<RunJob> runner;
  /** 计算对象 */
  private Future future;


  RunJob(Trigger trigger, Consumer<RunJob> runner) {
    this.trigger = trigger;
    this.runner = runner;
  }


  Future getFuture() {
    return future;
  }

  void setFuture(Future future) {
    this.future = future;
  }

  long nextCd() {
    return trigger.nextCd();
  }

  String getName() {
    return trigger.getName();
  }

  public Trigger getTrigger() {
    return trigger;
  }

  @Override
  public void run() {
    runner.accept(this);
  }
}
