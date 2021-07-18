package org.pj.core.serde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SerializerTest {

  /** 写入buf */
  private final ByteBuf buf = Unpooled.buffer();
  /** 测试对象 */
  private final CommonSerializer serializer = new CommonSerializer();


  @BeforeEach
  void before() {
    buf.clear();
  }

  @Test
  void serdeNullTest() {
    serializer.writeObject(buf, null);
    assertNull(serializer.read(buf));
  }

  @Test
  void byteSerdeTest() {
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      buf.clear();
      Byte v = (byte) i;
      serializer.writeObject(buf, v);
      assertEquals(v, serializer.read(buf));
    }
  }

  @Test
  void shortSerdeTest() {
    for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      buf.clear();
      Short v = (short) i;
      serializer.writeObject(buf, v);
      assertEquals(v, serializer.read(buf));
    }
  }

  @Test
  void intSerdeTest() {
    for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      buf.clear();
      Integer v = i;
      serializer.writeObject(buf, v);
      assertEquals(v, serializer.read(buf));
    }
  }

  @Test
  void longSerdeTest() {
    for (long i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      buf.clear();
      Long v = i;
      serializer.writeObject(buf, v);
      assertEquals(v, serializer.read(buf));
    }
  }

  @Test
  void floatSerdeTest() {
    for (float i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      buf.clear();
      Float v = i;
      serializer.writeObject(buf, v);
      assertEquals(v, serializer.read(buf));
    }
  }

  @Test
  void doubleSerdeTest() {
    for (double i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
      buf.clear();
      Double v = i;
      serializer.writeObject(buf, v);
      assertEquals(v, serializer.read(buf));
    }
  }

  @Test
  void charSerdeTest() {
    Character str = '中';
    serializer.writeObject(buf, str);
    assertEquals(str, serializer.read(buf));
  }

  @Test
  void strSerdeTest() throws NoSuchFieldException {
    String str = "Hello World!";
    serializer.writeObject(buf, str);
    assertEquals(str, serializer.read(buf));
  }
}
