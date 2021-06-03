package org.pj.game.conf;

import org.pj.game.conf.ConfigSystem.ConfigBuilder;

/**
 * 通用配置解析逻辑
 *
 * @author ZJP
 * @since 2020年06月05日 16:06:51
 **/
public interface IConfig {

  /**
   * 加载配置
   *
   * @since 2020年06月12日 09:31:13
   */
  void load(ConfigBuilder builder) throws Exception;

}
