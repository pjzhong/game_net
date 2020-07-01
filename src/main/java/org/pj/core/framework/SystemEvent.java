package org.pj.core.framework;

/**
 * 系统事件 [1-1024]
 *
 * @author ZJP
 * @since 2020年06月19日 17:00:08
 **/
public enum SystemEvent {
  // 连接断开
  CHANNEL_IN_ACTIVE(1022),
  //系统初始完成
  AFTER_INIT(1023),
  //系统启动成功
  SYSTEM_START(1024);

  int type;

  SystemEvent(int i) {
    this.type = i;
  }

  public int getType() {
    return type;
  }
}
