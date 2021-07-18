package org.pj.core.serde;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.pj.core.util.NettyByteBufUtil;

/**
 * 序列化组合实现,业务主要入口
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
 * String:8
 * Array:9
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
  public static final int ARRAY_ID = 9;

  private ArraySerializer arraySerializer;
  /** 序列化注册 [目标类型 -> 序列化实现] */
  private Map<Class<?>, Serializer<?>> serializers;
  /** [类型ID, 序列化实现] */
  private Map<Integer, Class<?>> id2Clazz;
  /** [具体类型, 类型ID] */
  private Map<Class<?>, Integer> clazz2Id;

  public CommonSerializer() {
    serializers = new HashMap<>();
    id2Clazz = new HashMap<>();
    clazz2Id = new HashMap<>();
    commonType();
    registerWrapper();
    arraySerializer = new ArraySerializer(this);

  }

  /**
   * 根据类型获取类型ID
   *
   * @param cls 类型
   * @since 2021年07月18日 16:18:08
   */
  public Integer getTypeId(Class<?> cls) {
    return clazz2Id.get(cls);
  }

  /**
   * 根据类型ID获取类型
   *
   * @param typeId 类型ID
   * @since 2021年07月18日 16:18:08
   */
  public Class<?> getClazz(Integer typeId) {
    return id2Clazz.get(typeId);
  }

  /**
   * 根据类型获取序列化实现
   *
   * @param cls 类型
   * @since 2021年07月18日 16:18:08
   */
  public <T> Serializer<T> getSerializer(Class<?> cls) {
    return (Serializer<T>) serializers.get(cls);
  }

  /**
   * 基础包装类型
   *
   * @since 2021年07月18日 10:31:21
   */
  private void registerWrapper() {
    serializers.put(Byte.class, serializers.get(Byte.TYPE));
    serializers.put(Short.class, serializers.get(Short.TYPE));
    serializers.put(Integer.class, serializers.get(Integer.TYPE));
    serializers.put(Long.class, serializers.get(Long.TYPE));
    serializers.put(Float.class, serializers.get(Float.TYPE));
    serializers.put(Double.class, serializers.get(Double.TYPE));
    serializers.put(Character.class, serializers.get(Character.TYPE));

    clazz2Id.put(Byte.class, BYTE_ID);
    clazz2Id.put(Short.class, SHORT_ID);
    clazz2Id.put(Integer.class, INTEGER_ID);
    clazz2Id.put(Long.class, LONG_ID);
    clazz2Id.put(Float.class, FLOAT_ID);
    clazz2Id.put(Double.class, DOUBLE_ID);
    clazz2Id.put(Character.class, CHARACTER_ID);
  }

  private void commonType() {
    registerSerializer(NULL_ID, NullSerializer.class, NullSerializer.INSTANCE);
    registerSerializer(BYTE_ID, Byte.TYPE, new ByteSerializer());
    registerSerializer(SHORT_ID, Short.TYPE, new ShortSerializer());
    registerSerializer(INTEGER_ID, Integer.TYPE, new IntegerSerializer());
    registerSerializer(LONG_ID, Long.TYPE, new LongSerializer());
    registerSerializer(FLOAT_ID, Float.TYPE, new FloatSerializer());
    registerSerializer(DOUBLE_ID, Double.TYPE, new DoubleSerializer());
    registerSerializer(CHARACTER_ID, Character.TYPE, new CharacterSerializer());
    registerSerializer(STRING_ID, String.class, new StringSerializer());
    registerSerializer(ARRAY_ID, ArraySerializer.class, new ArraySerializer(this));
  }


  /**
   * 注册序列化
   *
   * @param id 类型ID
   * @param clazz 类型
   * @since 2021年07月18日 11:37:14
   */
  public void registerSerializer(Integer id, Class<?> clazz) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(clazz);
    ObjectSerializer.checkClass(clazz);

    Serializer<?> serializer = new ObjectSerializer(clazz, this);
    registerSerializer(id, clazz, serializer);
  }


  /**
   * 注册序列化
   *
   * @param id 类型ID
   * @param clazz 类型
   * @param serializer 序列化实现
   * @since 2021年07月18日 11:37:14
   */
  public void registerSerializer(Integer id, Class<?> clazz, Serializer<?> serializer) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(serializer);

    serializers.put(clazz, serializer);
    Object old;
    if ((old = id2Clazz.put(id, clazz)) != null) {
      throw new RuntimeException(String.format("%s,%s 类型ID发生冲突", old, clazz));
    }
    clazz2Id.put(clazz, id);
  }


  /**
   * readObject 包装方法
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
      Class<?> clazz = id2Clazz.get(type);
      if (clazz == null) {
        throw new NullPointerException("类型ID:" + type + "，未注册");
      }
      Serializer<?> serializer = serializers.get(clazz);
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
    if (clazz.isArray()) {
      clazz = id2Clazz.get(ARRAY_ID);
    }

    int writeIdx = buf.writerIndex();
    try {
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
    } catch (Exception e) {
      buf.writerIndex(writeIdx);
      throw new RuntimeException("类型:" + clazz + ",序列化错误");
    }


  }
}
