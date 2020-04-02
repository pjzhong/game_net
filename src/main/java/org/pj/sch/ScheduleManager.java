package org.pj.sch;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhongjp
 * @since 2018年07月23日 10:32
 */
public class ScheduleManager implements AutoCloseable {


  private static ScheduleManager instance = new ScheduleManager();

  private final ScheduledThreadPoolExecutor manager;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private Map<String, RunJob> runJobMap = new ConcurrentHashMap<>();

  public ScheduleManager() {
    manager = createExecutor();
  }

  public static ScheduleManager getScheduleManager() {
    return instance;
  }

  private ScheduledThreadPoolExecutor createExecutor() {
    ThreadFactory factory = new ThreadFactory() {

      private AtomicInteger integer = new AtomicInteger();

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("Schedule-" + integer.incrementAndGet());
        return t;
      }
    };

    return new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), factory);
  }

  private void run(RunJob job) {
    Trigger trigger = job.getTrigger();
    try {
      final long now = System.currentTimeMillis();
      trigger.run();

      long elapsed = System.currentTimeMillis() - now;
      if (100 < elapsed) {
        logger.info("Trigger-{} run {}ms", job.getName(), elapsed);
      }
    } catch (Exception e) {
      logger.error("run " + job.getName(), e);
    }

    trigger.afterRun();

    if (0 <= job.nextCd() && runJobMap.get(job.getName()) == job) {
      schTrigger(job);
    } else {
      schCancel(job);
    }
  }

  public void schedule(Trigger t) {
    Objects.requireNonNull(t);
    Objects.requireNonNull(t.getName());

    RunJob jobRun = new RunJob(t, this::run);
    boolean duplicated = runJobMap.putIfAbsent(t.getName(), jobRun) != null;
    if (duplicated) {
      throw new RuntimeException("There duplicated jobs:" + t.getName());
    }

    schTrigger(jobRun);
  }

  public void schCancel(String name) {
    RunJob job = runJobMap.remove(name);
    Optional.ofNullable(job).map(RunJob::getFuture).ifPresent(f -> f.cancel(false));
  }

  private void schCancel(RunJob job) {
    boolean suc = runJobMap.remove(job.getName(), job);
    if (suc) {
      Optional.of(job).map(RunJob::getFuture).ifPresent(f -> f.cancel(false));
    }
  }

  private void schTrigger(RunJob t) {
    long next = t.nextCd();
    if (0 <= next) {
      Future f = manager.schedule(t, t.nextCd(), TimeUnit.MILLISECONDS);
      t.setFuture(f);
    }
  }

  @Override
  public void close() {
    manager.shutdown();
  }
}
