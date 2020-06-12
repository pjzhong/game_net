package org.pj.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManager {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private Map<Integer, List<CallBack>> callbacks;
  private ExecutorService executorService;

  public EventManager() {
    callbacks = new ConcurrentHashMap<>();
    executorService = ForkJoinPool.commonPool();
  }

  public EventManager(ExecutorService service) {
    callbacks = new ConcurrentHashMap<>();
    executorService = service;
  }

  public void registerEvent(Object target) {
    //TODO REGISTER
  }

  public void fireEvent(int type, Object... params) {
    List<CallBack> calls = callbacks.get(type);
    if (calls == null) {
      logger.info("No Subscribe for {}", type);
      return;
    }

    for (CallBack c : calls) {
      try {
        c.handle(params);
      } catch (Exception e) {
        logger.error("fireEvent[{}] error, e:{}", type, e.getCause());
      }
    }
  }

  public void asyncFireEvent(int type, Object... params) {
    List<CallBack> calls = callbacks.get(type);
    if (calls == null) {
      logger.info("No Subscribe for {}", type);
      return;
    }

    for (CallBack callBack : calls) {
      executorService.execute(() -> {
        try {
          callBack.handle(params);
        } catch (Exception e) {
          logger.error("fireEvent[{}] error, e:{}", type, e.getCause());
        }
      });
    }
  }


}
