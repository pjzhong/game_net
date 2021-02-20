package org.pj.boot;

import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.pj.core.framework.SpringGameContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GameBoot {

  public static void main(String[] argc) throws Exception {
    GameBoot.start(argc);
  }

  private SpringGameContext gameCtx;
  private AnnotationConfigApplicationContext springCtx;

  private GameBoot() {
  }

  private GameBoot(SpringGameContext gameCtx,
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

  public static GameBoot start() throws Exception {
    return GameBoot.start(ArrayUtils.EMPTY_STRING_ARRAY);
  }

  public static GameBoot start(String... argc) throws Exception {
    try {
      return doStart(argc);
    } catch (Throwable e) {
      throw new RuntimeException("Game start failed", e);
    }
  }

  private static GameBoot doStart(String[] argc) throws Exception {
    GameBoot boot = new GameBoot();
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
