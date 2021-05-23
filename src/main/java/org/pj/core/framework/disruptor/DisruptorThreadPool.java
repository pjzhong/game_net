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
  private Disruptor<RunnableWrapper>[] pools;
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
    EventHandler<RunnableWrapper> handler = new GameRunnableHandler();
    pools = new Disruptor[poolSize];
    for (int i = 0; i < poolSize; i++) {
      Disruptor<RunnableWrapper> disruptor = new Disruptor<>(RunnableWrapper::new, buffSize,
          factory);
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
    int nextIdx = nextIndex.getAndIncrement();
    if (pools.length <= nextIdx) {
      nextIndex.compareAndSet(nextIdx, 0);
      nextIdx = nextIdx % pools.length;
    }
    return nextIdx;
  }

  public void exec(String name, Runnable runnable) {
    Disruptor<RunnableWrapper> disruptor = pools[ThreadLocalRandom.current().nextInt(pools.length)];
    disruptor.getRingBuffer().publishEvent((event, sequence, r, n) -> {
      event.setRunnable(r);
      event.setName(n);
    }, runnable, name);
  }


  public void exec(Channel o, String name, Runnable runnable) {
    Disruptor<RunnableWrapper> disruptor = pools[bind(o)];
    disruptor.getRingBuffer().publishEvent((event, sequence, r, n) -> {
      event.setRunnable(r);
      event.setName(n);
    }, runnable, name);
  }

  private Attribute<Integer> poolIdx(Channel o) {
    return o.attr(poolIdx);
  }

  public void shutdown() throws InterruptedException {
    for (Disruptor<RunnableWrapper> e : pools) {
      e.shutdown();
    }
  }
}
