/** 广州哇宝信息技术有限公司 */
package org.pj.module.item.config;


import org.pj.module.conf.Config;
import org.pj.module.item.ItemType;

/**
 * 物品基类模板
 *
 * @author PCY
 * @since 2017年4月7日 上午11:31:44
 */
public abstract class AbsTemplate {

  /** ID */
  @Config
  public int id;
  /** 物品类型 */
  @Config
  public int type;
  /** 名称 */
  @Config
  public String name;
  /** 品质 */
  @Config
  public int quality;

  public AbsTemplate() {
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getQuality() {
    return quality;
  }

  public int getType() {
    return type;
  }

  /**
   * 获取物品的类型
   *
   * @author PCY
   * @since 2017年4月7日 下午6:11:00
   */
  public ItemType getItemType() {
    return null;
  }
}
