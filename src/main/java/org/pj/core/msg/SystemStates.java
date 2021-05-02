package org.pj.core.msg;

public final class SystemStates {

  /** 协议号不存在 */
  public static final int MODULE_404 = 100;

  public static final int OK = 200;

  public static final int SYSTEM_ERR = 500;

  private SystemStates() {
    throw new UnsupportedOperationException();
  }
}