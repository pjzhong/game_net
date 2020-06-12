package org.pj.core.sch;

/**
 * 定时器
 *
 * @author zhongjp
 * @since 2018年07月23日 10:07
 */
public interface Trigger extends Runnable {


  /**
   * 定时器名字
   *
   * @since 2020年03月19日 23:50:57
   */
  String getName();

  /**
   * 获取下次运行时间
   *
   * @return 下次运行时间 已结束:-1
   * @since 2020年03月19日 23:50:11
   */
  long nextCd();


  /**
   * 运行前
   *
   * @since 2020年03月25日 00:05:21
   */
  void beforeRun();

  /**
   * 运行后
   *
   * @since 2020年03月25日 00:05:35
   */
  void afterRun();


  /**
   * 取消运行
   *
   * @since 2020年03月25日 00:16:26
   */
  void cancel();

}
