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
    int stop = 10;
    AtomicInteger integer = new AtomicInteger();
    manager.schedule(
        new SimpleTrigger("interval", System.currentTimeMillis(), TimeUnit.SECONDS.toMillis(1),
            integer::incrementAndGet));

    TimeUnit.SECONDS.sleep(stop);
    Assert.assertEquals(integer.intValue(), stop);
  }


  @Test
  public void timeOut() throws InterruptedException {
    ScheduleManager manager = new ScheduleManager();
    int stop = 10;
    int delay = 5;
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
    int repeat = 2;
    AtomicInteger integer = new AtomicInteger();
    manager.schedule(
        new SimpleTrigger("interval", System.currentTimeMillis(),
            repeat, TimeUnit.SECONDS.toMillis(2), integer::incrementAndGet));

    TimeUnit.SECONDS.sleep(11);

    Assert.assertEquals(integer.intValue(), repeat);
  }

}
