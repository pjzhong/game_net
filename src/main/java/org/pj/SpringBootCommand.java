package org.pj;

import org.pj.core.framework.SpringGameContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringBootCommand implements CommandLineRunner {

  private SpringGameContext gameContext;

  @Autowired
  public SpringBootCommand(SpringGameContext gameContext) {
    this.gameContext = gameContext;
  }

  @Override
  public void run(String... args) throws Exception {
    gameContext.start();
    gameContext.registerShutdownHook();
  }
}
