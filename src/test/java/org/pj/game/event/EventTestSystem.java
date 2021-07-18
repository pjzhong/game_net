package org.pj.game.event;

import org.pj.core.framework.ISystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventTestSystem implements ISystem {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  public volatile int number = 0;

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
