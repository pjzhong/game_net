package org.pj.module.event;

import org.pj.core.event.Listen;
import org.pj.core.framework.ISystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventTestSystem implements ISystem {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  public int number = 0;

  @Listen(100)
  public void init(int num) {
    number = num;
    logger.debug("EventTestSystem 1 event trigger");
  }

  @Listen(200)
  public void add(int add) {
    number += add;
    logger.debug("EventTestSystem 2 event trigger");
  }

  @Override
  public void load() throws Exception {

  }

  @Override
  public void init() {

  }

  @Override
  public void destroy() {

  }
}
