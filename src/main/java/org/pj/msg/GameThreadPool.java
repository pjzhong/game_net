package org.pj.msg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.pj.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameThreadPool {

  private static final int MIN_SIZE = 4;
  private static final int MAX_SIZE = 8;

  private final int limit;
  private final ExecutorService[] pools;
  private final Map<Integer, Long> hashStat;

  private Logger logger = LoggerFactory.getLogger(this.getClass());

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
    int idx = o.hashCode() & limit;
    hashStat.merge(idx, 1L, Long::sum);
    return pools[idx];
  }

  public Map<Integer, Long> getHashStat() {
    return hashStat;
  }
}
