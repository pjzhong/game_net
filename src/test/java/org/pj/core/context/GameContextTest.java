package org.pj.core.context;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GameContextTest {

  private static SpringGameContext context;

  @BeforeClass
  public static void init() {
    long start = System.currentTimeMillis();
    context = new SpringGameContext();
    context.setContext(new AnnotationConfigApplicationContext(ServerConfig.class));
    context.start();
    System.out.println("cost:" + (System.currentTimeMillis() - start));
  }

  @AfterClass
  public static void end() throws Exception {
    context.close();
  }

  @Test
  public void echoHelloWorldTest() throws Exception {
  }

}
