package org.pj.core.framework.cross;


import org.pj.core.msg.Message;

public interface ResultCallBack<T> extends SocketCallback<T> {


  /**
   * 错误处理
   *
   * @param message 消息
   * @since 2020年07月06日 08:10:21
   */
  void acceptErr(Message message);

  /**
   * 回调异常处理
   *
   * @param exp 回调异常
   * @since 2020年07月05日 18:18:20
   */
  void onException(Exception exp);

}
