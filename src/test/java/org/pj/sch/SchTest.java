package org.pj.sch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.pj.sch.trigger.SimpleTrigger;

/**
 * Unit interval for simple App.
 */
public class SchTest {

  @Test
  public void interval() throws InterruptedException {
    ScheduleManager manager = new ScheduleManager();
    int stop = 1;
    AtomicInteger integer = new AtomicInteger();
    manager.schedule(
        new SimpleTrigger("interval", System.currentTimeMillis(), TimeUnit.SECONDS.toMillis(1),
            integer::incrementAndGet));

    TimeUnit.SECONDS.sleep(stop);
    Assert.assertTrue(stop <= integer.intValue());
  }


  @Test
  public void timeOut() throws InterruptedException {
    ScheduleManager manager = new ScheduleManager();
    int stop = 2;
    int delay = 1;
    AtomicInteger integer = new AtomicInteger();
    manager.schedule(
        new SimpleTrigger("interval",
            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delay),
            integer::incrementAndGet));

    TimeUnit.SECONDS.sleep(stop);
    Assert.assertEquals(integer.intValue(), 1);
  }

  @Test
  public void repeat() throws InterruptedException {
    ScheduleManager manager = new ScheduleManager();
    int repeat = 1;
    AtomicInteger integer = new AtomicInteger();
    manager.schedule(
        new SimpleTrigger("interval", System.currentTimeMillis(),
            repeat, TimeUnit.SECONDS.toMillis(repeat), integer::incrementAndGet));

    TimeUnit.SECONDS.sleep(repeat * repeat + 1);

    Assert.assertEquals(integer.intValue(), repeat);
  }

}
