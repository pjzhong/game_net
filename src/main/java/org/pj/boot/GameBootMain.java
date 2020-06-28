package org.pj.boot;

import org.pj.core.framework.SpringGameContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GameBootMain {

  public static void main(String[] argc) throws Exception {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
        ServerConfig.class);
    SpringGameContext context = ctx.getBean(SpringGameContext.class);
    context.start();
    context.registerShutdownHook();
  }

}
