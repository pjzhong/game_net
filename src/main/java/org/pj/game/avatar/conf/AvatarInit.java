package org.pj.game.avatar.conf;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.pj.game.conf.Config;
import org.pj.game.conf.ConfigTable;
import org.pj.game.util.GameUtil;

/**
 * 角色基础配置
 *
 * @author xchao
 * @since 2019年4月11日 下午4:41:39
 */
@ConfigTable("init")
public class AvatarInit {

  /** 角色初始等级 */
  public static int avatarLvInit = 1;
  /** 角色名最小长度 */
  @Config
  public static int nameMin;
  /** 角色名最大长度 */
  @Config
  public static int nameMax;
  /** 角色初始元宝 */
  @Config
  public static int initGold;
  /** 角色初始金币 */
  @Config
  public static int initMoney;
  /** 初始化VIP */
  @Config
  public static int initVip;
  /** 改名花费 */
  @Config
  public static int renameCost;
  /** 改名道具 */
  @Config
  public static int renameItem;
  /** 阵容格子解锁需求 */
  @Config(parser = "fillBattleArray")
  public static Map<Integer, Pos> battleArray;

  /** 男性初始头像 */
  @Config(alias = "avatarMaleIcon1")
  public static String maleIcon;
  /** 女性初始头像 */
  @Config(alias = "avatarFemaleIcon1")
  public static String femaleIcon;

  /** 男性初始皮肤 */
  @Config(alias = "avatarMaleSkin1")
  public static int maleSkin;
  /** 男性初始骑姿皮肤 */
  @Config(alias = "avatarMaleSkin2")
  public static int maleSkinHorse;
  /** 女性初始皮肤 */
  @Config(alias = "avatarFemaleSkin1")
  public static int femaleSkin;
  /** 女性初始骑姿皮肤 */
  @Config(alias = "avatarFemaleSkin2")
  public static int femaleSkinHorse;
  @Config(parser = "fillidMap")
  public static Map<Integer, Double> avatarQualityGrow;
  /** 角色的初始品质 */
  @Config
  public static int avatarQuality;
  /** 主角默认武器模型 */
  @Config
  public static int avatarWeapon;
  /** 充值积分 */
  @Config
  public static int rechargePoint;
  /** 充值换算成元宝的比例，只用于计算活动数据 */
  @Config
  public static int rechargeMultiPoint;

  @Config(parser = "toPairList", parserClass = GameUtil.class)
  public static List<Pair<Integer, Long>> initReward;
  /** 主角等级上限 */
  @Config
  public static int maxLv;
  /** 达到等级上限后溢出的经验转化为铜钱 */
  @Config
  public static float changeCoin;
  /** 免费跳过次数 */
  @Config
  public static int freeSkip;
  /** 实名认证奖励 */
  @Config(parser = "toPairList", parserClass = GameUtil.class)
  public static List<Pair<Integer, Long>> realNameGift;
  /** 初始精力值 */
  @Config
  public static long initUndergo;

  public static double getRateByQua(int qua) {
    return avatarQualityGrow.getOrDefault(qua, 1.0);
  }

  public Map<Integer, Double> fillidMap(String str) {
    Map<Integer, Double> map = new HashMap<>();
    if (!StringUtils.isBlank(str)) {
      for (String s : str.split(";")) {
        String[] as = s.split(":");
        map.put(Integer.parseInt(as[0]), Double.parseDouble(as[1]));
      }
    }
    return map;
  }

  public Map<Integer, Pos> fillBattleArray(String str) {
    Map<Integer, Pos> map = new HashMap<>();
    if (!StringUtils.isBlank(str)) {
      int i = 1;
      for (String s : str.split("\\|")) {
        String[] as = s.split(",");
        Pos p = new Pos(i, Integer.parseInt(as[0]),
            Integer.parseInt(as[1]));
        map.put(i, p);
        i++;
      }
    }
    return map;

  }

  /**
   * 阵容格子解锁信息
   */
  public static class Pos {

    /** 第几个格子 */
    private int pos;
    /** 解锁等级 */
    private int lv;
    /** 解锁vip等级 */
    private int vip;

    public Pos(int pos, int lv, int vip) {
      super();
      this.pos = pos;
      this.lv = lv;
      this.vip = vip;
    }

    public int getPos() {
      return pos;
    }

    public int getLv() {
      return lv;
    }

    public int getVip() {
      return vip;
    }

  }
}
