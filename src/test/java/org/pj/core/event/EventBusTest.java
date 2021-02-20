package org.pj.core.event;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pj.boot.GameBoot;
import org.pj.core.framework.SpringGameContext;
import org.pj.module.event.EventTestSystem;

public class EventBusTest {

  private static int INIT_EVENT = 100;
  private static int ADD_EVENT = 200;

  @Test
  public void simpleTest() {
    EventBus bus = new EventBus();
    EventTestSystem testSystem = new EventTestSystem();
    bus.registerEvent(testSystem);

    bus.fireEvent(INIT_EVENT, 1);
    Assertions.assertEquals(testSystem.number, 1);

    bus.fireEvent(ADD_EVENT, 1);
    Assertions.assertEquals(testSystem.number, 2);
  }

  @Test
  public void simpleAsyncTest() throws InterruptedException {
    EventBus bus = new EventBus();
    EventTestSystem testSystem = new EventTestSystem();
    bus.registerEvent(testSystem);

    bus.asyncFireEvent(INIT_EVENT, 1);
    TimeUnit.MILLISECONDS.sleep(1);
    Assertions.assertEquals(testSystem.number, 1);

    bus.asyncFireEvent(ADD_EVENT, 1);
    TimeUnit.MILLISECONDS.sleep(1);
    Assertions.assertEquals(testSystem.number, 2);
  }

  @Test
  public void withGameContext() throws Exception {
    GameBoot localBoot = GameBoot.start();
    SpringGameContext gameContext = localBoot.getGameCtx();

    EventTestSystem testSystem = gameContext.getBean(EventTestSystem.class);

    gameContext.fireEvent(INIT_EVENT, 1);
    Assertions.assertEquals(testSystem.number, 1);

    gameContext.fireEvent(ADD_EVENT, 1);
    Assertions.assertEquals(testSystem.number, 2);

    gameContext.close();
    localBoot.getSpringCtx().close();
  }

}
