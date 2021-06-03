package org.pj.core.framework;

import javax.annotation.PostConstruct;
import org.pj.game.avatar.conf.AvatarConfig;
import org.pj.game.conf.ConfigSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class SystemAdapter implements ISystem {

  @Autowired
  private ConfigSystem configSystem;

  private AvatarConfig avatarConfig = AvatarConfig.getInstance();


  @Override
  @PostConstruct
  public void load() throws Exception {
    avatarConfig.load(configSystem.builderWithPrefix("avatar"));
  }

  @Override
  public void init() {
  }

  @Override
  public void destroy() {
  }

}
