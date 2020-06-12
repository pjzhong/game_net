package org.pj.core.sch.trigger;


import java.util.Objects;
import java.util.concurrent.Future;
import org.pj.core.sch.Trigger;

/**
 * 普通定时器
 *
 * @author zhongjp
 * @since 2018年07月23日 11:09
 */
public class SimpleTrigger implements Trigger {

  /** 定时器名字 */
  private String name;
  /** 执行间隔 */
  private long period;
  /** 下次执行时间 */
  private long next;
  /** 结束时间 */
  private long end;
  /** 定时任务 */
  private Runnable task;
  /** 已取消 */
  private boolean running;
  /** 计算结果 */
  private Future future;

  public SimpleTrigger(String name, long start, Runnable task) {
    this(name, start, -1, start, task);
  }

  public SimpleTrigger(String name, long start, int repeat, long period, Runnable task) {
    this(name, start, period, start + repeat * period, task);
  }

  public SimpleTrigger(String name, long start, long period, Runnable task) {
    this(name, start, period, Long.MAX_VALUE, task);
  }


  public SimpleTrigger(String name, long start, long period, long endTime, Runnable task) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(task);
    if (start < 0 || start == Long.MAX_VALUE) {
      throw new IllegalArgumentException("must be 0 <= init < Long.MAX_VALUE");
    }

    this.name = name;
    this.next = start;
    this.end = endTime;
    this.task = task;
    this.period = period;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long nextCd() {
    if (0 < next) {
      return Math.max(0, next - System.currentTimeMillis());
    } else {
      return -1;
    }
  }

  @Override
  public void beforeRun() {
    //DO Nothing
  }

  @Override
  public void afterRun() {
    long nextPeriod = System.currentTimeMillis() + period;
    if (nextPeriod < end && 0 < period) {
      next = nextPeriod;
    } else {
      next = -1;
    }
  }

  @Override
  public void cancel() {
    end = 0;
    if (future != null) {
      future.cancel(false);
    }
  }

  @Override
  public void run() {
    task.run();
  }
}
