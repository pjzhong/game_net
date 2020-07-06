package org.pj.core.framework.cross;

/**
 * 信息回调
 *
 * @author ZJP
 * @since 2020年07月02日 09:55:25
 **/
public interface SocketCallback<T> {

  /**
   * 请求成功的回调(此成功为网络层的成功)
   *
   * @param t 返回的数据
   * @since 2020年07月02日 09:58:14
   */
  void accept(T t);

}
