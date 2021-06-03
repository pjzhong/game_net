package org.pj;

import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.pj.config.CrossServerConfig;
import org.pj.config.ServerConfig;
import org.pj.core.framework.SpringGameContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class GameBootTest {

  public static void main(String[] argc) throws Exception {
    GameBootTest.start(argc);
  }

  private SpringGameContext gameCtx;
  private AnnotationConfigApplicationContext springCtx;

  private GameBootTest() {
  }

  private GameBootTest(SpringGameContext gameCtx,
      AnnotationConfigApplicationContext springCtx) {
    this.gameCtx = gameCtx;
    this.springCtx = springCtx;
  }

  public SpringGameContext getGameCtx() {
    return gameCtx;
  }

  public AnnotationConfigApplicationContext getSpringCtx() {
    return springCtx;
  }

  public void close() throws Exception {
    gameCtx.close();
    springCtx.close();
  }

  public static GameBootTest start() throws Exception {
    return GameBootTest.start(ArrayUtils.EMPTY_STRING_ARRAY);
  }

  public static GameBootTest start(String... argc) throws Exception {
    try {
      return doStart(argc);
    } catch (Throwable e) {
      throw new RuntimeException("Game start failed", e);
    }
  }

  private static GameBootTest doStart(String[] argc) throws Exception {
    GameBootTest boot = new GameBootTest();
    boolean cross = Arrays.asList(argc).contains("cross");
    AnnotationConfigApplicationContext ctx;
    if (cross) {
      ctx = new AnnotationConfigApplicationContext(CrossServerConfig.class);
    } else {
      ctx = new AnnotationConfigApplicationContext(ServerConfig.class);
    }
    String[] scans = ctx.getEnvironment().getProperty("game.scans", "").split(",");
    ctx.scan(scans);

    SpringGameContext context = ctx.getBean(SpringGameContext.class);
    context.start();
    context.registerShutdownHook();

    boot.gameCtx = context;
    boot.springCtx = ctx;
    return boot;
  }

}
