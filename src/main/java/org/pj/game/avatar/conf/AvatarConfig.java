package org.pj.game.avatar.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.pj.game.conf.ConfigSystem.ConfigBuilder;
import org.pj.game.conf.IConfig;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class AvatarConfig implements IConfig {

  private static AvatarConfig instance = new AvatarConfig();

  public static AvatarConfig getInstance() {
    return instance;
  }

  private Map<Integer, LevelUp> levelUps;

  /** VIP配置 */
  private Map<Integer, Vip> vipMap;
  /** first name */
  public List<String> firstName;
  /** middle name */
  public List<String> middleName;
  /** last name */
  public List<String> maleLastName;
  /** 女性 最后名字 */
  public List<String> femaleLastName;

  /** 称号ID -> 称号配置 */
  private Map<Integer, AppearanceConfig> titles;
  /** 聊天气泡ID->聊天气泡配置 */
  private Map<Integer, AppearanceConfig> bubblesMap;
  /** 聊天头像框ID->聊天头像框配置 */
  private Map<Integer, AppearanceConfig> headCaseMap;
  /** 聊天头衔ID->聊天头衔配置 */
  private Map<Integer, AppearanceConfig> appellationMap;
  /** 聊天头像ID->聊天头像配置 */
  private Map<Integer, AppearanceConfig> headMap;

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
    vipMap = builder.loadValuesMap(Vip::getLevel, Vip.class);

    // 角色名
    SqlRowSet record = builder.getTemplate().queryForRowSet("SELECT * FROM avatar_name");
    List<String> firstName = new ArrayList<>();
    List<String> middleName = new ArrayList<>();
    List<String> maleLastName = new ArrayList<>();
    List<String> femaleLastName = new ArrayList<>();
    while (record.next()) {
      String first = record.getString("first");
      if (StringUtils.isNotBlank(first)) {
        firstName.add(first);
      }
      String middle = record.getString("mid");
      if (StringUtils.isNotBlank(middle)) {
        middleName.add(middle);
      }
      String last = record.getString("last");
      if (StringUtils.isNotBlank(last)) {
        maleLastName.add(last);
      }
      String femaleLast = record.getString("femalelast");
      if (StringUtils.isNotBlank(femaleLast)) {
        femaleLastName.add(femaleLast);
      }
    }
    this.firstName = firstName;
    this.middleName = middleName;
    this.maleLastName = maleLastName;
    this.femaleLastName = femaleLastName;

    // 称号配置
    titles = builder.loadValuesMap("avatar_title", AppearanceConfig::getId, AppearanceConfig.class);
    bubblesMap = builder.loadValuesMap("avatar_bubble", AppearanceConfig::getId, AppearanceConfig.class);
    headCaseMap = builder.loadValuesMap("avatar_headcase", AppearanceConfig::getId, AppearanceConfig.class);
    appellationMap = builder.loadValuesMap("avatar_appellation", AppearanceConfig::getId, AppearanceConfig.class);
    headMap = builder.loadValuesMap("avatar_head", AppearanceConfig::getId, AppearanceConfig.class);
  }
}
