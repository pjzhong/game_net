package org.pj.core.framework.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.pj.common.NamedThreadFactory;

public class DisruptorThreadPool {

  /** 线程池 */
  private Disruptor<GameRunnable>[] pools;
  /** 线程池地址 */
  private AttributeKey<Integer> poolIdx = AttributeKey.valueOf("Pool-Index");
  /** 下一个线程池 */
  private AtomicInteger nextIndex;

  public DisruptorThreadPool() {
    this(Runtime.getRuntime().availableProcessors(), 4096,
        new NamedThreadFactory("game--disruptor-thread"));
  }

  public DisruptorThreadPool(int poolSize, int buffSize, NamedThreadFactory factory) {
    nextIndex = new AtomicInteger(0);
    EventHandler<GameRunnable> handler = new GameRunnableHandler();
    pools = new Disruptor[poolSize];
    for (int i = 0; i < poolSize; i++) {
      Disruptor<GameRunnable> disruptor = new Disruptor<>(GameRunnable::new, buffSize, factory);
      disruptor.handleEventsWith(handler);
      disruptor.start();
      pools[i] = disruptor;
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


  public void exec(Channel o, Runnable runnable) {
    Attribute<Integer> idxAttr = poolIdx(o);
    Disruptor<GameRunnable> disruptor = pools[idxAttr.get() == null ? ThreadLocalRandom.current()
        .nextInt(pools.length) : idxAttr.get()];
    disruptor.getRingBuffer().publishEvent(RunnableTranslator.INSTANCE, runnable);
  }

  private Attribute<Integer> poolIdx(Channel o) {
    return o.attr(poolIdx);
  }

  public void shutdown() throws InterruptedException {
    for (Disruptor e : pools) {
      e.shutdown();
    }
  }
}
