package org.pj.core.framework;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameThreadPool {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** 线程池 */
  private final ExecutorService[] pools;
  /** 线程池地址 */
  private AttributeKey<Integer> poolIdx = AttributeKey.valueOf("Pool-Index");
  /** 下一个线程池 */
  private AtomicInteger nextIndex;

  public GameThreadPool() {
    this(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("game-thread"));
  }

  public GameThreadPool(int poolSize, NamedThreadFactory factory) {
    nextIndex = new AtomicInteger(0);
    pools = new ExecutorService[poolSize];
    for (int i = 0; i < poolSize; i++) {
      pools[i] = Executors.newSingleThreadExecutor(factory);
    }
  }

  public int bind(Channel o) {
    Attribute<Integer> idx = poolIdx(o);
    if (idx.get() == null) {
      idx.setIfAbsent(nextPoolIdx());
    }
    return idx.get();
  }

  private int nextPoolIdx() {
    int nextIdx = nextIndex.incrementAndGet();
    if (pools.length <= nextIdx) {
      nextIndex.compareAndSet(nextIdx, 0);
      nextIdx = nextIdx % pools.length;
    }
    return nextIdx;
  }

  public ExecutorService nextExecutorService() {
    return pools[nextPoolIdx()];
  }

  /**
   * 使用@param o的哈希值来决定线程池
   *
   * @param o 随机对象
   * @since 2020年04月02日 20:51:49
   */
  public ExecutorService getPool(Channel o) {
    Integer idx = poolIdx(o).get();
    return pools[idx == null ? ThreadLocalRandom.current().nextInt(pools.length) : idx];
  }

  private Attribute<Integer> poolIdx(Channel o) {
    return o.attr(poolIdx);
  }

  public void shutdown() throws InterruptedException {
    for (ExecutorService e : pools) {
      e.shutdown();
      e.awaitTermination(10, TimeUnit.SECONDS);
      logger.debug("{} isShutdown", e, e.isShutdown());
      e.shutdownNow();
    }
  }
}
