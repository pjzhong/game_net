package org.pj.module.avatar;

import java.util.Map;
import java.util.Map.Entry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.GameBoot;
import org.pj.core.framework.SpringGameContext;
import org.pj.module.avatar.conf.AvatarConfig;
import org.pj.module.avatar.conf.AvatarInit;
import org.pj.module.avatar.conf.LevelUp;
import org.pj.module.conf.ConfigSystem;

public class AvatarSystemTest {

  private static GameBoot boot;

  @BeforeClass
  public static void init() throws Exception {
    boot = GameBoot.start();

  }

  @AfterClass
  public static void close() {
    boot.getGameCtx().close();
    boot.getSpringCtx().close();
  }

  @Test
  public void configTest() throws Exception {
    SpringGameContext context = boot.getGameCtx();
    AvatarConfig config = AvatarConfig.getInstance();
    ConfigSystem configSystem = context.getBean(ConfigSystem.class);
    config.load(configSystem.builderWithPrefix("avatar"));

    Assert.assertNotNull(AvatarInit.realNameGift);
    System.out.println(AvatarInit.realNameGift);

    Map<Integer, LevelUp> levelUpMap = config.getLevelUps();
    // Assert.assertTrue(ObjectUtils.isNotEmpty(config.getLevelUps()));

    for (Entry<Integer, LevelUp> entry : levelUpMap.entrySet()) {
      Integer k = entry.getKey();
      LevelUp v = entry.getValue();
      //   Assert.assertEquals(k.intValue(), v.getLevel());
    }
  }

}
