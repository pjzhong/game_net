package org.pj.module.avatar.conf;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.pj.module.conf.Config;
import org.pj.module.conf.ConfigTable;
import org.pj.module.util.GameUtil;

/**
 * 等级配置
 *
 * @author dhy
 * @since 2018年4月19日 上午10:59:29
 */
@ConfigTable(value = "level")
public class LevelUp {

  /** 等级 */
  @Config
  private int level;
  /** 经验 */
  @Config
  private long experiences;
  /** 升到本级奖励道具 */
  @Config(parser = "toPairList", parserClass = GameUtil.class)
  private List<Pair<Integer, Long>> item;
  /** 历练上限 */
  @Config
  private long undergoMax;


  public int getLevel() {
    return level;
  }

  public long getExp() {
    return experiences;
  }

  public List<Pair<Integer, Long>> getItem() {
    return item;
  }

  public long getUndergoMax() {
    return undergoMax;
  }
}
