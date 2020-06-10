package org.pj.module.avatar;

import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pj.boot.AppConfig;
import org.pj.module.avatar.conf.AvatarConfig;
import org.pj.module.avatar.conf.AvatarInit;
import org.pj.module.avatar.conf.LevelUp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AvatarSystemTest {

  private static ApplicationContext context;

  @BeforeClass
  public static void init() {
    context = new AnnotationConfigApplicationContext(AppConfig.class);
  }

  @Test
  public void configTest() throws Exception {
    AvatarConfig config = AvatarConfig.getInstance();
    config.load(context);

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
