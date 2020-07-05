package org.pj.core.framework.cross;

/**
 * 信息回调
 *
 * @author ZJP
 * @since 2020年07月02日 09:55:25
 **/
public interface SocketCallback<T> {

  /**
   * 请求成功的回调
   *
   * @param t 返回的数据
   * @since 2020年07月02日 09:58:14
   */
  void onSuccess(T t);

  /**
   * 请求失败的时的调用
   *
   * @param var1 对应异常
   * @since 2020年07月02日 09:58:39
   */
  void onError(Exception var1);

}
