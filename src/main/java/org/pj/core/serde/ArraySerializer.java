package org.pj.core.serde;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.util.Objects;
import org.pj.core.util.NettyByteBufUtil;

/**
 * 通用数据序列化
 *
 * <pre>
 *   一维数组:
 *    +---------+--------+--------+-----+----+
 *    + 维度总数 | 维度1长 | 类型ID  |元素1|元素1|
 *    +--------+--------+--------+-----+----+
 *    维度总数:1-5字节, 使用varint32编码
 *    维度1长:1-5字节, 使用varint32编码
 *    类型ID:1-5字节, 使用varint32编码
 *    元素:实现决定
 * </pre>
 *
 * 与{@link CommonSerializer} 组合使用
 *
 * @author ZJP
 * @since 2021年07月18日 14:17:04
 **/
public class ArraySerializer implements Serializer<Object> {

  private CommonSerializer serializer;

  public ArraySerializer(CommonSerializer serializer) {
    this.serializer = serializer;
  }

  /**
   * 获取数据维度
   *
   * @param array 数据
   * @return int[] 获取数据每个维度的长度, 如果是不规则的数据则抛出异常
   * @since 2021年07月18日 17:01:34
   */
  static int[] getDimensions(Object array) {
    Objects.requireNonNull(array);

    int dimensions = 0;
    for (Class<?> next = array.getClass().getComponentType(); next != null;
        next = next.getComponentType()) {
      dimensions += 1;
    }

    int[] lengthOfDimension = new int[dimensions];
    lengthOfDimension[0] = Array.getLength(array);
    if (1 < dimensions) {
      collectDimensions(array, 1, lengthOfDimension);
    }

    return lengthOfDimension;
  }

  /**
   * 填充每个维度的长度
   *
   * @param array 目标数据
   * @param dimension 当前维度
   * @param dimensions 维度记录
   * @since 2021年07月18日 17:12:22
   */
  static void collectDimensions(Object array, int dimension, int[] dimensions) {
    boolean elementsAreArrays = dimension < dimensions.length - 1;
    for (int i = 0, s = Array.getLength(array); i < s; ++i) {
      Object element = Array.get(array, i);
      if (element != null) {
        int length = Array.getLength(element);
        if (dimensions[dimension] == 0) {
          dimensions[dimension] = length;
        } else if (dimensions[dimension] != length) {
          throw new RuntimeException("多维度数据，长度不一致");
        }
        if (elementsAreArrays) {
          collectDimensions(element, dimension + 1, dimensions);
        }
      }
    }
  }

  @Override
  public Object readObject(ByteBuf buf) {
    int dimensionCount = NettyByteBufUtil.readInt32(buf);
    if (dimensionCount <= 0) {
      return null;
    } else {
      int[] dimensions = new int[dimensionCount];
      for (int i = 0; i < dimensionCount; i++) {
        int dimension = NettyByteBufUtil.readInt32(buf);
        if (dimension < 0) {
          throw new RuntimeException("wrong dimensions [" + i + "]" + dimension);
        }

        dimensions[i] = dimension;
      }

      final int typeId = NettyByteBufUtil.readInt32(buf);
      Class<?> componentType = serializer.getClazz(typeId);
      if (componentType == null) {
        throw new RuntimeException("类型ID:" + typeId + ",没有注册");
      }

      Serializer<Object> eleSerializer = serializer.getSerializer(componentType);
      if (eleSerializer == null) {
        throw new RuntimeException("类型:" + componentType + ",没有序列化实现");
      }

      Object array = Array.newInstance(componentType, dimensions);
      readArray(eleSerializer, buf, array, 0, dimensions);
      return array;
    }
  }

  @Override
  public void writeObject(ByteBuf buf, Object object) {
    if (!object.getClass().isArray()) {
      throw new RuntimeException("类型:" + object.getClass() + ",不是数组");
    }
    int[] dimensions = getDimensions(object);

    NettyByteBufUtil.writeInt32(buf, dimensions.length);
    for (int i : dimensions) {
      NettyByteBufUtil.writeInt32(buf, i);
    }

    Class<?> componentType = object.getClass().getComponentType();
    while (componentType.getComponentType() != null) {
      componentType = componentType.getComponentType();
    }

    Integer typeId = serializer.getTypeId(componentType);
    if (typeId == null) {
      throw new RuntimeException("类型:" + object.getClass() + ",没有注册");
    }
    NettyByteBufUtil.writeInt32(buf, typeId);
    Serializer<Object> elementSerializer = serializer.getSerializer(componentType);
    writeArray(buf, object, elementSerializer, 0, dimensions);
  }

  /**
   * 把数组序列化至{@param buff}
   *
   * @param buf 目标buff
   * @param array 目标数据
   * @param serializer 序列化实现
   * @param dim 维度
   * @param dimensions 各维度长度
   * @since 2021年07月18日 20:17:35
   */
  private void writeArray(ByteBuf buf, Object array, Serializer<Object> serializer, int dim,
      int[] dimensions) {
    int length = dimensions[dim];

    final boolean elementsAreArrays = dim < dimensions.length - 1;
    for (int i = 0; i < length; ++i) {
      Object element = Array.get(array, i);
      if (elementsAreArrays) {
        writeArray(buf, element, serializer, dim + 1, dimensions);
      } else {
        serializer.writeObject(buf, element);
      }
    }
  }

  private void readArray(Serializer<Object> eleSerializer, ByteBuf buf, Object array, int dim,
      int[] dimensions) {
    boolean elementAreArrays = dim < dimensions.length - 1;
    int length = dimensions[dim];
    for(int i = 0; i < length; ++i) {
      if(elementAreArrays) {
        Object element = Array.get(array, i);
        readArray(eleSerializer, buf,  element, dim + 1, dimensions);
      } else {
        Array.set(array, i, eleSerializer.readObject(buf));
      }
    }
  }
}