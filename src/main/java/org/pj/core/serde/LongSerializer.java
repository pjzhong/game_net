package org.pj.core.serde;

import io.netty.buffer.ByteBuf;
import org.pj.core.util.NettyByteBufUtil;

/**
 * Integer序列化实现(使用varint32和ZigZag32进行编码)
 *
 * 与{@link CommonSerializer} 组合使用
 *
 * @since 2021年07月17日 16:16:14
 **/
public class LongSerializer implements Serializer<Long> {

  @Override
  public Long readObject(ByteBuf buf) {
    return NettyByteBufUtil.readInt64(buf);
  }

  @Override
  public void writeObject(ByteBuf buf, Long object) {
    NettyByteBufUtil.writeInt64(buf, object);
  }
}
