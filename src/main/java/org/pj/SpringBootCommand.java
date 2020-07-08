package org.pj;

import org.pj.core.framework.SpringGameContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringBootCommand implements CommandLineRunner {

  @Autowired
  private GenericApplicationContext context;

  @Override
  public void run(String... args) throws Exception {
    SpringGameContext gameContext = context.getBean(SpringGameContext.class);
    gameContext.start();
  }
}
