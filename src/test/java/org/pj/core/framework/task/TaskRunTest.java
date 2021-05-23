package org.pj.core.framework.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pj.core.framework.disruptor.DisruptorThreadPool;
import org.pj.core.framework.task.wrapper.GameTaskWrapper;

public class TaskRunTest {


  private static DisruptorThreadPool pool;

  @BeforeAll
  private static void init() {
    pool = new DisruptorThreadPool();
  }

  @AfterAll
  private static void end() throws InterruptedException {
    pool.shutdown();
  }

  @Test
  public void oneArg() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    pool.exec("await", GameTaskWrapper.of(CountDownLatch::countDown, latch));
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void twoArg() throws InterruptedException {
    CountDownLatch A = new CountDownLatch(1);
    CountDownLatch B = new CountDownLatch(1);
    pool.exec("await", GameTaskWrapper.of((a, b) -> {
      a.countDown();
      b.countDown();
    }, A, B));
    assertTrue(A.await(1, TimeUnit.SECONDS));
    assertTrue(B.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void threeArg() throws InterruptedException {
    CountDownLatch A = new CountDownLatch(1);
    CountDownLatch B = new CountDownLatch(1);
    CountDownLatch C = new CountDownLatch(1);
    pool.exec("await", GameTaskWrapper.of((a, b, c) -> {
      a.countDown();
      b.countDown();
      c.countDown();
    }, A, B, C));
    assertTrue(A.await(1, TimeUnit.SECONDS));
    assertTrue(B.await(1, TimeUnit.SECONDS));
    assertTrue(C.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void varArg() throws InterruptedException {
    CountDownLatch A = new CountDownLatch(1);
    CountDownLatch B = new CountDownLatch(1);
    CountDownLatch C = new CountDownLatch(1);
    CountDownLatch D = new CountDownLatch(1);
    pool.exec("await", GameTaskWrapper.ofArgs((args) -> {
      for (Object cdl : args) {
        CountDownLatch l = (CountDownLatch) cdl;
        l.countDown();
      }
    }, A, B, C, D));
    assertTrue(A.await(1, TimeUnit.SECONDS));
    assertTrue(B.await(1, TimeUnit.SECONDS));
    assertTrue(C.await(1, TimeUnit.SECONDS));
    assertTrue(D.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void oneArgRecycleTest() {
    Runnable runnable = GameTaskWrapper.of((a) -> {
    }, "A");
    runnable.run();
    Runnable after = GameTaskWrapper.of((a) -> {
    }, "A");
    assertEquals(runnable, after);
    after.run();
  }

  @Test
  public void twoArgRecycleTest() {
    Runnable runnable = GameTaskWrapper.of((a, b) -> {
    }, "A", "B");
    runnable.run();

    Runnable after = GameTaskWrapper.of((a, b) -> {
    }, "A", "B");
    assertEquals(runnable, after);
    after.run();
  }

  @Test
  public void threeArgRecycleTest() {
    Runnable runnable = GameTaskWrapper.of((a, b, c) -> {
    }, "A", "B", "C");
    runnable.run();

    Runnable after = GameTaskWrapper.of((a, b, c) -> {
    }, "A", "B", "C");
    assertEquals(runnable, after);
    after.run();
  }

  @Test
  public void varArgRecycleTest() throws InterruptedException {
    Runnable runnable = GameTaskWrapper.ofArgs((args) -> {
    }, "A", "B", "C", "D");
    runnable.run();

    Runnable after = GameTaskWrapper.ofArgs((args) -> {
    }, "A", "B", "C", "D");
    assertEquals(runnable, after);
    after.run();
  }

}
