package org.pj.core.framework;

/**
 * 游戏系统接口
 *
 * @author ZJP
 * @since 2020年06月12日 11:46:06
 **/
public interface ISystem {

  /**
   * 加载系统配置, 方法的实现应支持重复执行以应对配置变更的情况
   *
   * @since 2020年06月12日 11:46:04
   */
  void load() throws Exception;

  /**
   * 系统初始化
   *
   * @since 2020年06月12日 11:47:05
   */
  void init();

  /**
   * 系统销毁
   *
   * @since 2020年06月12日 11:47:25
   */
  void destroy();
}
