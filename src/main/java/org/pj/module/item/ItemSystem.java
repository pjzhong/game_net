package org.pj.module.item;

import org.pj.core.framework.SystemAdapter;
import org.pj.module.conf.ConfigSystem;
import org.pj.module.item.config.ItemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemSystem extends SystemAdapter {

  @Autowired
  private ConfigSystem configSystem;

  private ItemConfig itemConfig = ItemConfig.getInstance();

  @Override
  public void load() throws Exception {
    itemConfig.load(configSystem.builderWithPrefix("item"));
  }

}
