package org.pj.core.msg;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.pj.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NettyChannelThreadPool {

  private static final int MIN_SIZE = 4;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  /** 线程池 */
  private final ExecutorService[] pools;
  /** 线程池地址 */
  private AttributeKey<Integer> poolIdx = AttributeKey.valueOf("Pool-Index");
  /** 获取统计 */
  private final Map<Integer, Long> poolBinds;

  public NettyChannelThreadPool() {
    this(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("game-thread"));
  }

  public NettyChannelThreadPool(int poolSize, NamedThreadFactory factory) {
    int size = tableSizeFore(poolSize);
    pools = new ExecutorService[size];
    poolBinds = new ConcurrentHashMap<>(size);
    for (int i = 0; i < size; i++) {
      pools[i] = Executors.newSingleThreadExecutor(factory);
      poolBinds.put(i, 0L);
    }
  }

  /**
   * copy from java.util.HashMap#tableSizeFor(int)
   */
  private int tableSizeFore(int cap) {
    int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
    n = (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? MIN_SIZE : n + 1;
    return n;
  }

  public int bind(Channel o) {
    Attribute<Integer> idx = poolIdx(o);
    if (idx.get() == null) {
      Entry<Integer, Long> min = Collections
          .min(poolBinds.entrySet(), Comparator.comparingLong(Entry::getValue));
      idx.setIfAbsent(min.getKey());
      poolBinds.merge(min.getKey(), 1L, Long::sum);
    }
    return idx.get();
  }

  public void unbind(Channel o) {
    Integer idx = poolIdx(o).get();
    if (idx != null) {
      poolBinds.merge(idx, -1L, Long::sum);
    }
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

  public Map<Integer, Long> getPoolBinds() {
    return poolBinds;
  }

  public void shutdown() throws InterruptedException {
    for (ExecutorService e : pools) {
      e.shutdown();
      e.awaitTermination(10, TimeUnit.SECONDS);
      logger.debug("{} isShutdown", e, e.isShutdown());
      e.shutdownNow();
    }
    poolBinds.clear();
  }
}
