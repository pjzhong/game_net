package org.pj.module.avatar.conf;

import java.util.Map;
import org.pj.module.conf.AbsConfig;

public class AvatarConfig extends AbsConfig {

  private static AvatarConfig instance = new AvatarConfig();

  public static AvatarConfig getInstance() {
    return instance;
  }

  private Map<Integer, LevelUp> levelUps;

  private AvatarConfig() {
  }

  @Override
  public void setValue() throws Exception {
    // 初始化全局变量配置
    loadInit(AvatarInit.class);

    levelUps = loadValuesMap(LevelUp::getLevel, LevelUp.class);
  }

  public Map<Integer, LevelUp> getLevelUps() {
    return levelUps;
  }

  @Override
  public String prefix() {
    return "avatar";
  }
}
