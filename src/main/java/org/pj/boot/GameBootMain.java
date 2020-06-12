package org.pj.boot;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GameBootMain {

  public static void main(String[] argc) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
        ServerConfig.class);
    ctx.registerShutdownHook();
  }

}
