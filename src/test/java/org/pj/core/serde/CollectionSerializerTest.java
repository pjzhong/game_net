package org.pj.core.serde;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * 集合序列化测试
 *
 * @author ZJP
 * @since 2021年07月18日 14:17:04
 **/
public class CollectionSerializerTest {

  /** 业务入口 */
  private CommonSerializer serializer;
  /** 临时buff */
  private ByteBuf buf;

  @BeforeEach
  void beforeEach() {
    serializer = new CommonSerializer();
    buf = Unpooled.buffer();

    CollectionSerializer collectSer = new CollectionSerializer(serializer);

    serializer.registerSerializer(10, Collection.class, collectSer);
    serializer.linkTo(List.class, Collection.class);
    serializer.linkTo(ArrayList.class, Collection.class);
  }

  @Test
  void intCollectionTest() {
    Random random = ThreadLocalRandom.current();
    List<Integer> col = new ArrayList<>();
    int size = random.nextInt(Short.MAX_VALUE);
    for (int i = 0; i < size; i++) {
      col.add(1);
    }

    serializer.writeObject(buf, col);
    List<Integer> res = serializer.read(buf);
    Assertions.assertEquals(col, res);
  }

  @Test
  void doubleCollectionTest() {
    Random random = ThreadLocalRandom.current();
    List<Double> col = new ArrayList<>();
    int size = random.nextInt(Short.MAX_VALUE);
    for (int i = 0; i < size; i++) {
      col.add(random.nextDouble());
    }

    serializer.writeObject(buf, col);
    List<Integer> res = serializer.read(buf);
    Assertions.assertEquals(col, res);
  }

  @Test
  void strCollectionTest() {
    Random random = ThreadLocalRandom.current();
    List<String> col = new ArrayList<>();
    int size = random.nextInt(Short.MAX_VALUE);
    for (int i = 0; i < size; i++) {
      col.add(Integer.toString(random.nextInt()));
    }

    col.set(random.nextInt(size), null);

    serializer.writeObject(buf, col);
    List<Integer> res = serializer.read(buf);
    Assertions.assertEquals(col, res);
  }

  @Test
  void linkListError() {
    List<Integer> list = new LinkedList<>();
    Assertions.assertThrows(RuntimeException.class, () -> serializer.writeObject(buf, list));
  }


}
