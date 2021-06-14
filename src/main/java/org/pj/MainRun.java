package org.pj;

import org.pj.core.framework.SpringGameContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MainRun implements CommandLineRunner {

  private final SpringGameContext gameContext;

  @Autowired
  public MainRun(SpringGameContext gameContext) {
    this.gameContext = gameContext;
  }

  @Override
  public void run(String... args) throws Exception {
    gameContext.registerShutdownHook();
    gameContext.start();

  }
}
