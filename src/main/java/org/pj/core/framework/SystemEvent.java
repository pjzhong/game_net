package org.pj.core.framework;

/**
 * 系统事件 [1-1024]
 *
 * @author ZJP
 * @since 2020年06月19日 17:00:08
 **/
public enum SystemEvent {
  ALL_SYSTEM_INIT(1);

  int type;

  SystemEvent(int i) {
    this.type = i;
  }

  public int getType() {
    return type;
  }
}
