package org.pj.core.serde;


import io.netty.buffer.ByteBuf;

/**
 * 编码解码器接口
 *
 * <pre>
 *    +--------+-----------------+
 *    + 类型ID  | "内       容" |
 *    +--------+----------------+
 *    类型ID长度:1-5字节, 使用varint32编码
 *    内容长度:根据实现来确定
 * </pre>
 *
 * @author ZJP
 * @since 2021年07月17日 15:59:16
 **/
public interface Serializer<T> {

  /**
   * 从{@param buff}反序列化对象
   *
   * @param buf 目标buff
   * @since 2021年07月17日 16:02:03
   */
  T readObject(ByteBuf buf);

  /**
   * 把{@param object}序列化至{@param buff}
   *
   * @param buf 目标buff
   * @param object 对象
   * @since 2021年07月17日 16:02:03
   */
  void writeObject(ByteBuf buf, T object);
}
