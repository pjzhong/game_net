package org.pj.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.pj.common.NamedThreadFactory;

public class GameThreadPool {

  private static final int MIN_SIZE = 4;
  private static final int MAX_SIZE = 8;

  /** 线程池上限 */
  private final int limit;
  /** 线程池 */
  private final ExecutorService[] pools;
  /** 获取统计 */
  private final Map<Integer, Long> hashStat;

  public GameThreadPool() {
    this(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("game-thread"));
  }

  public GameThreadPool(int poolSize, NamedThreadFactory factory) {
    int size = tableSizeFore(poolSize);
    limit = size - 1;
    pools = new ExecutorService[size];
    hashStat = new ConcurrentHashMap<>(size);
    for (int i = 0; i < size; i++) {
      pools[i] = Executors.newSingleThreadExecutor(factory);
    }
  }

  /**
   * copy from java.util.HashMap#tableSizeFor(int)
   */
  private int tableSizeFore(int cap) {
    int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
    n = (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? MIN_SIZE : n + 1;
    return Math.min(n, MAX_SIZE);
  }

  /**
   * 使用@param o的哈希值来决定线程池
   *
   * @param o 随机对象
   * @since 2020年04月02日 20:51:49
   */
  public ExecutorService getPool(Object o) {
    int h = System.identityHashCode(o.hashCode());
    int idx = (h ^ (h >>> 16)) & limit;
    hashStat.merge(idx, 1L, Long::sum);
    return pools[idx];
  }

  public Map<Integer, Long> getHashStat() {
    return hashStat;
  }
}
