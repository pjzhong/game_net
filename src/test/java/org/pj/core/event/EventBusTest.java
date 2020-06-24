package org.pj.core.event;

import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.pj.boot.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.pj.module.event.EventTestSystem;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class EventBusTest {

  private static int INIT_EVENT = 100;
  private static int ADD_EVENT = 200;

  @Test
  public void simpleTest() {
    EventBus bus = new EventBus();
    EventTestSystem testSystem = new EventTestSystem();
    bus.registerEvent(testSystem);

    bus.fireEvent(INIT_EVENT, 1);
    Assert.assertEquals(testSystem.number, 1);

    bus.fireEvent(ADD_EVENT, 1);
    Assert.assertEquals(testSystem.number, 2);
  }

  @Test
  public void simpleAsyncTest() throws InterruptedException {
    EventBus bus = new EventBus();
    EventTestSystem testSystem = new EventTestSystem();
    bus.registerEvent(testSystem);

    bus.asyncFireEvent(INIT_EVENT, 1);
    TimeUnit.MILLISECONDS.sleep(1);
    Assert.assertEquals(testSystem.number, 1);

    bus.asyncFireEvent(ADD_EVENT, 1);
    TimeUnit.MILLISECONDS.sleep(1);
    Assert.assertEquals(testSystem.number, 2);
  }

  @Test
  public void withGameContext() throws Exception {
    GenericApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);
    SpringGameContext gameContext = context.getBean(SpringGameContext.class);
    gameContext.start();

    EventTestSystem testSystem = gameContext.getBean(EventTestSystem.class);

    gameContext.fireEvent(INIT_EVENT, 1);
    Assert.assertEquals(testSystem.number, 1);

    gameContext.fireEvent(ADD_EVENT, 1);
    Assert.assertEquals(testSystem.number, 2);

    gameContext.asyncFireEvent(INIT_EVENT, 1);
    TimeUnit.MILLISECONDS.sleep(1);
    Assert.assertEquals(testSystem.number, 1);

    gameContext.asyncFireEvent(ADD_EVENT, 1);
    TimeUnit.MILLISECONDS.sleep(1);
    Assert.assertEquals(testSystem.number, 2);

    gameContext.close();
  }

}
