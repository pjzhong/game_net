package org.pj.game.item.config;


import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.pj.game.conf.Config;
import org.pj.game.conf.ConfigTable;
import org.pj.game.util.GameUtil;

@ConfigTable("item")
public class ItemTemplate extends AbsTemplate {

  /** 背包分类 */
  @Config
  private int bagType;
  /** 类型参数 */
  @Config(allowNull = true)
  private String chooseGift;
  /** 能否使用 */
  @Config
  private int canUse;
  /** 能否批量使用 */
  @Config
  private int batchUse;
  /** 批量使用最大数量 */
  @Config
  private int batchUseNum;
  /** 掉落ID */
  @Config(allowNull = true)
  private int drop;
  /** 时效类型 */
  @Config
  private int timeType;
  /** 使用数量限制 */
  @Config
  private int heroLimit;
  /** 英雄碎片检测 */
  @Config(allowNull = true)
  private Integer herocheck;
  /** 自动使用类型 */
  @Config
  private int automaticUse;
  /** 外观id */
  @Config(allowNull = true)
  private int avatarId;
  /** 转化物品 */
  @Config(parser = "toPairList", parserClass = GameUtil.class)
  private List<Pair<Integer, Long>> conversion;

  public int getBatchUseNum() {
    return batchUseNum;
  }

  public boolean canUse() {
    if (canUse == 1) {
      return true;
    }
    return false;
  }

  public boolean canBatchUse() {
    if (batchUse == 1) {
      return true;
    }
    return false;
  }

  public int getCanUse() {
    return canUse;
  }

  public Pair<Integer, Long> toUseNeedItem(String param) {
    return GameUtil.toPair(param);
  }

  public Pair<Integer, Long> parseSell(String param) {
    return GameUtil.toPair(param);
  }


  public int getTimeType() {
    return timeType;
  }

  public int getBatchUse() {
    return batchUse;
  }

  public int getDrop() {
    return drop;
  }

  public int getBagType() {
    return bagType;
  }

  public String getChooseGift() {
    return chooseGift;
  }

  public int getHeroLimit() {
    return heroLimit;
  }

  public Integer getHerocheck() {
    return herocheck;
  }


  public List<Pair<Integer, Long>> getConversion() {
    return conversion;
  }

  public int getAvatarId() {
    return avatarId;
  }
}
