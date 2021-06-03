package org.pj.game.avatar.conf;


import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.pj.game.conf.Config;
import org.pj.game.conf.ConfigTable;
import org.pj.game.util.GameUtil;

/**
 * vip配置
 *
 * @since 2015年3月7日 下午5:47:21
 */
@ConfigTable("vip")
public class Vip {

  /** 等级 */
  @Config
  private int level;
  /** 升到下一级所需VIP积分 */
  @Config
  private int rechargeLimit;

  /**
   * 快速作战次数上限（默认1次）
   */
  @Config
  private int handFastTime;
  /** 日常副本的挑战/扫荡额外次数 */
  @Config(alias = "materialTimes")
  private int dailyCopyExtraTimes;
  /** 竞技场挑战次数 */
  @Config
  private int arenaBuy;
  /** vip对应，用元宝购买铜钱的次数 */
  @Config
  private int buyResource;
  /** 群英传记战令额外购买次数 */
  @Config
  private int heroPicTimes;
  /** 野外BOSS购买次数 */
  @Config(alias = "yewaiBuy")
  private int fieldBossBuy;
  /** 全民BOSS购买次数 */
  @Config(alias = "quanminBuy")
  private int allBossBuy;
  /** 军团科技免费跳过时间（单位分钟） */
  @Config
  private int techFree;
  /** 挂机离线收益时长(单位分钟) */
  @Config
  private int outGameTimes;
  /** 挂机离线收益时长(单位分钟) */
  @Config(alias = "ShopTimes")
  private int shopTimes;
  /** 异族BOSS购买次数 */
  @Config(alias = "yizulaixiBuy")
  private int alienBossBuy;
  /** 远征试炼购买 */
  @Config(alias = "resetNum")
  private int expeditionReset;
  @Config(alias = "function5")
  private int skipWorldBoss;

  /** 摇钱树次数 */
  @Config(alias = "moneyTreeBuy")
  private int moneyTreeTotal;
  /** 每日转生经验购买次数 */
  @Config
  private int reincarnationNum;
  /** 每天可购买修炼点次数 */
  @Config
  private int buyPracticeSpotNum;
  /** 每天可购买射箭次数 */
  @Config
  private int drillBuy1;
  /** 每天可购买步兵次数 */
  @Config
  private int drillBuy2;
  /** 每天可购买骑兵次数 */
  @Config
  private int drillBuy3;
  /** 神装副本可购买次数 */
  @Config
  private int godfbBuy;
  /** 兵法副本扫荡购买次数 */
  @Config(alias = "baqiWipe")
  private int baqiSweep;
  /** Vip礼包 */
  @Config(parser = "parsePairList")
  private List<Pair<Integer, Long>> vipGift;
  /** 礼包实际价格 */
  @Config(parser = "toPair", parserClass = GameUtil.class)
  private Pair<Integer, Long> truePrice;
  /** 借船次数 */
  @Config
  private int arrowTimes;
  /** 水镜副本锦囊额外购买次数 */
  @Config
  private int sjPicTimes;
  /** 每天可以购买灵宠仙域的次数 */
  @Config
  private int petfbBuy;
  /** 每天可以购买镜像对决的次数 */
  @Config
  private int shadowBuy;
  /** 每天购买合服boss的次数 */
  @Config(alias = "margeBuy")
  private int mergeBossBuy;
  /** 结义竞技场挑战次数购买 */
  @Config
  private int swornArenaBuy;

  public Pair<Integer, Long> fillPairList(String str) {
    return GameUtil.toPair(str);
  }

  public int getLevel() {
    return level;
  }

  public int getRechargeLimit() {
    return rechargeLimit;
  }

  public int getHandFastTime() {
    return handFastTime;
  }

  public List<Pair<Integer, Long>> getVipGift() {
    return vipGift;
  }

  public int getDailyCopyExtraTimes() {
    return dailyCopyExtraTimes;
  }

  public int getArenaBuy() {
    return arenaBuy;
  }

  public Pair<Integer, Long> getTruePrice() {
    return truePrice;
  }

  public int getBuyResource() {
    return buyResource;
  }

  public int getHeroPicTimes() {
    return heroPicTimes;
  }

  public int getFieldBossBuy() {
    return fieldBossBuy;
  }

  public int getAllBossBuy() {
    return allBossBuy;
  }

  public int getTechFree() {
    return techFree;
  }

  public int getOutGameTimes() {
    return outGameTimes;
  }

  public int getShopTimes() {
    return shopTimes;
  }

  public int getAlienBossBuy() {
    return alienBossBuy;
  }

  public int getExpeditionReset() {
    return expeditionReset;
  }

  public boolean isSkipWorldBoss() {
    return skipWorldBoss == 1;
  }

  public int getMoneyTreeTotal() {
    return moneyTreeTotal;
  }

  public List<Pair<Integer, Long>> parsePairList(String str) {
    List<Pair<Integer, Long>> list = new ArrayList<>();
    if (StringUtils.isNotBlank(str)) {
      for (String s : str.split("\\|")) {
        list.add(GameUtil.toPair(s));
      }
    }
    return list;
  }

  public int getArrowTimes() {
    return arrowTimes;
  }

  public int getReincarnationNum() {
    return reincarnationNum;
  }

  public int getBuyPracticeSpotNum() {
    return buyPracticeSpotNum;
  }

  public int getGodfbBuy() {
    return godfbBuy;
  }

  public int getSjPicTimes() {
    return sjPicTimes;
  }

  public int getBaqiSweep() {
    return baqiSweep;
  }

  public int getPetfbBuy() {
    return petfbBuy;
  }

  public int getShadowBuy() {
    return shadowBuy;
  }

  public int getMergeBossBuy() {
    return mergeBossBuy;
  }

  public int getSwornArenaBuy() {
    return swornArenaBuy;
  }
}
