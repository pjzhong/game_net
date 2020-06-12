package org.pj.module.avatar.conf;

import java.util.Map;
import org.pj.module.conf.ConfigSystem.ConfigBuilder;
import org.pj.module.conf.IConfig;

public class AvatarConfig implements IConfig {

  private static AvatarConfig instance = new AvatarConfig();

  public static AvatarConfig getInstance() {
    return instance;
  }

  private Map<Integer, LevelUp> levelUps;

  private AvatarConfig() {
  }

  public Map<Integer, LevelUp> getLevelUps() {
    return levelUps;
  }

  @Override
  public void load(ConfigBuilder builder) throws Exception {
    // 初始化全局变量配置
    builder.loadInit(AvatarInit.class);
    levelUps = builder.loadValuesMap(LevelUp::getLevel, LevelUp.class);
  }
}
