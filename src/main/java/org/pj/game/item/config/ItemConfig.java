package org.pj.game.item.config;

import java.util.HashMap;
import java.util.Map;
import org.pj.game.conf.ConfigSystem.ConfigBuilder;
import org.pj.game.conf.IConfig;

/**
 * 物品配置
 *
 * @author PCY
 * @since 2017年4月7日 上午11:32:47
 */
public class ItemConfig implements IConfig {

  private static ItemConfig itemConfig = new ItemConfig();
  /** 所有 */
  private Map<Integer, AbsTemplate> absMap;
  /** 物品 */
  private Map<Integer, ItemTemplate> itemMap;


  private ItemConfig() {
  }

  public static ItemConfig getInstance() {
    return itemConfig;
  }

  public Map<Integer, AbsTemplate> getAbsMap() {
    return absMap;
  }

  public AbsTemplate getAbsTemplate(int itemId) {
    return absMap.get(itemId);
  }

  public ItemTemplate getItemTemp(int tempId) {
    return itemMap.get(tempId);
  }


  @Override
  public void load(ConfigBuilder builder) throws Exception {
    this.absMap = new HashMap<>();
    this.itemMap = builder.loadValuesMap(ItemTemplate::getId, ItemTemplate.class);

    absMap.putAll(itemMap);
  }
}
