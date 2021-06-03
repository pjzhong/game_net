package org.pj.game;

/**
 * 持久化数据接口
 *
 * @author yaowenhao
 * @date 2014年7月23日 下午6:18:47
 */
public interface DBData {

  /** 数据检查和初始化扩展字段(相对原始构造方法) */
  void check();
}
