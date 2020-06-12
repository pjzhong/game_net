package org.pj.module.avatar;

import javax.annotation.PostConstruct;
import org.pj.core.framework.ISystem;
import org.pj.module.avatar.conf.AvatarConfig;
import org.pj.module.conf.ConfigSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 角色系统
 *
 * @author ZJP
 * @since 2020年06月12日 10:36:41
 **/
@Component
public class AvatarSystem implements ISystem {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ConfigSystem configSystem;
  private AvatarConfig config = AvatarConfig.getInstance();


  @Override
  @PostConstruct
  public void load() throws Exception {
    config.load(configSystem.builderWithPrefix("avatar"));
  }

  @Override
  public void init() {

  }

  @Override
  public void destroy() {

  }
}
