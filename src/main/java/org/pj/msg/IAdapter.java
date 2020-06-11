package org.pj.msg;

/**
 * 参数适配器
 *
 * @author ZJP
 * @since 2020年06月11日 10:00:18
 **/
public interface IAdapter<T> {

  /**
   * @param context 调用上下文
   * @param info 处理器信息
   * @param idx 参数下标
   * @since 2020年06月11日 10:00:10
   */
  T adapter(InvokeContext context, HandlerInfo info, int idx) throws Exception;
}
