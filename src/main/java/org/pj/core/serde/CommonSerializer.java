package org.pj.core.serde;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.pj.core.util.NettyByteBufUtil;

/**
 * 通用对象序列化,业务主要入口
 *
 * <pre>
 * 基本类型ID:
 * null:0
 * Byte:1
 * Short:2
 * Integer:3
 * Long:4
 * Float:5
 * Double:6
 * Character:7
 * String:7
 * </pre>
 *
 * @author ZJP
 * @since 2021年07月17日 16:30:05
 **/
public class CommonSerializer implements Serializer<Object> {

  /** 通用类型ID */
  public static final int NULL_ID = 0;
  public static final int BYTE_ID = 1;
  public static final int SHORT_ID = 2;
  public static final int INTEGER_ID = 3;
  public static final int LONG_ID = 4;
  public static final int FLOAT_ID = 5;
  public static final int DOUBLE_ID = 6;
  public static final int CHARACTER_ID = 7;
  public static final int STRING_ID = 8;

  /** 序列化注册 [目标类型 -> 序列化实现] */
  private Map<Class<?>, Serializer<?>> serializers;
  /** [类型ID, 序列化实现] */
  private Map<Integer, Serializer<?>> id2Serializer;
  /** [具体类型, 类型ID] */
  private Map<Class<?>, Integer> clazz2Id;

  public CommonSerializer() {
    serializers = new HashMap<>();
    id2Serializer = new HashMap<>();
    clazz2Id = new HashMap<>();
    commonType();
  }

  private void commonType() {
    registerSerializer(NULL_ID, NullSerializer.class, NullSerializer.INSTANCE);
    registerSerializer(BYTE_ID, Byte.class, new ByteSerializer());
    registerSerializer(SHORT_ID, Short.class, new ShortSerializer());
    registerSerializer(INTEGER_ID, Integer.class, new IntegerSerializer());
    registerSerializer(LONG_ID, Long.class, new LongSerializer());
    registerSerializer(FLOAT_ID, Float.class, new FloatSerializer());
    registerSerializer(DOUBLE_ID, Double.class, new DoubleSerializer());
    registerSerializer(CHARACTER_ID, Character.class, new CharacterSerializer());
    registerSerializer(STRING_ID, String.class, new StringSerializer());
  }

  public void registerSerializer(Integer id, Class<?> clazz, Serializer<?> serializer) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(serializer);

    serializers.put(clazz, serializer);
    Object old;
    if ((old = id2Serializer.put(id, serializer)) != null) {
      throw new RuntimeException(String.format("%s,%s 类型ID发生冲突", old, clazz));
    }
    clazz2Id.put(clazz, id);
  }


  /**
   * readObject 包装类
   *
   * @param buf 目标buff
   * @since 2021年07月17日 16:36:14
   */
  @SuppressWarnings("unchecked")
  public <T> T read(ByteBuf buf) {
    return (T) readObject(buf);
  }

  @Override
  public Object readObject(ByteBuf buf) {
    int readerIndex = buf.readerIndex();
    try {
      int type = NettyByteBufUtil.readInt32(buf);
      Serializer<?> serializer = id2Serializer.get(type);
      if (serializer == null) {
        throw new NullPointerException("类型ID:" + type + "，未注册");
      }
      return serializer.readObject(buf);
    } catch (Exception e) {
      buf.readerIndex(readerIndex);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeObject(ByteBuf buf, Object object) {
    Class<?> clazz = object == null ? NullSerializer.class : object.getClass();
    @SuppressWarnings("unchecked")
    Serializer<Object> serializer = (Serializer<Object>) serializers.get(clazz);
    if (serializer == null) {
      throw new RuntimeException("类型:" + clazz + "，未注册");
    }

    Integer id = clazz2Id.get(clazz);
    if (id == null) {
      throw new RuntimeException("类型:" + clazz + "，没有类型ID");
    }

    NettyByteBufUtil.writeInt32(buf, id);
    serializer.writeObject(buf, object);
  }
}
