package org.pj.core.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBus {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private Map<Integer, Set<Subscriber>> callbacks;

  public EventBus() {
    callbacks = new ConcurrentHashMap<>();
  }

  public void registerEvent(Object target) {
    Class<?> clazz = target.getClass();
    Method[] methods = clazz.getDeclaredMethods();

    for (Method m : methods) {
      Listen listen = m.getAnnotation(Listen.class);
      if (listen != null) {
        if (0 < listen.value()) {
          Subscriber info = new Subscriber(target, m);
          callbacks.computeIfAbsent(listen.value(), k -> new HashSet<>()).add(info);
          logger.debug("{}.{} subscribe event {}", clazz.getName(), m.getName(), listen.value());
        } else {
          logger.error("{}.{} illegal event {}", clazz.getName(), m.getName(), listen.value());
        }

      }
    }
  }

  public void fireEvent(int type, Object... params) {
    Set<Subscriber> calls = getListeners(type);
    for (Subscriber c : calls) {
      try {
        c.handle(params);
      } catch (Exception e) {
        logger
            .error(String.format("fireEvent[%s] error, param:%s", type, Arrays.toString(params)),
                e.getCause());
      }
    }
  }

  public void asyncFireEvent(int type, Object... params) {
    asyncFireEvent(ForkJoinPool.commonPool(), type, params);
  }

  public void asyncFireEvent(ExecutorService service, int type, Object... params) {
    Set<Subscriber> calls = getListeners(type);

    for (Subscriber callBack : calls) {
      service.execute(() -> {
        try {
          callBack.handle(params);
        } catch (Exception e) {
          logger
              .error(String.format("fireEvent[%s] error, param:%s", type, Arrays.toString(params)),
                  e.getCause());
        }
      });
    }
  }

  private Set<Subscriber> getListeners(int type) {
    Set<Subscriber> calls = callbacks.getOrDefault(type, Collections.emptySet());
    if (calls.isEmpty()) {
      logger.info("No Subscribe for {}", type);
    }
    return calls;
  }


}
