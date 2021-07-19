package org.pj.core.serde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectSerializerTest {

  private CommonSerializer serializer;
  private ByteBuf buf;

  @BeforeEach
  void before() {
    serializer = new CommonSerializer();
    buf = Unpooled.buffer();
  }

  @Test
  void checkTest() {
    //primitive check
    assertThrows(RuntimeException.class, () -> ObjectSerializer.checkClass(Integer.TYPE));

    //constructor check
    assertThrows(RuntimeException.class,
        () -> ObjectSerializer.checkClass(NoDefaultConstructor.class));
    Assertions.assertDoesNotThrow(
        () -> ObjectSerializer.checkClass(DefaultConstructor.class));

    //abstract class check
    assertThrows(RuntimeException.class, () -> ObjectSerializer.checkClass(List.class));

    //annotation class check
    assertThrows(RuntimeException.class, () -> ObjectSerializer.checkClass(Override.class));

    //abstract class check
    assertThrows(RuntimeException.class,
        () -> ObjectSerializer.checkClass(AbstractList.class));

    //unregister check
    assertThrows(RuntimeException.class,
        () -> serializer.writeObject(buf, new DefaultConstructor()));
  }

  @Test
  void primitiveTest() {
    PrimitiveObj obj = new PrimitiveObj();
    obj.d = Double.MIN_VALUE;

    serializer.registerSerializer(10, PrimitiveObj.class);
    serializer.writeObject(buf, obj);

    PrimitiveObj res = serializer.read(buf);
    assertEquals(obj, res);
  }

  @Test
  void wrapperTest() {
    WrapperObj obj = new WrapperObj();
    obj.d = Double.MIN_VALUE;
    obj.str = "Hello World!";
    obj.b = null;

    serializer.registerSerializer(10, WrapperObj.class);
    serializer.writeObject(buf, obj);

    WrapperObj res = serializer.read(buf);
    assertEquals(obj, res);
  }

  @Test
  void composeTest() {
    serializer.registerSerializer(10, ComposeObj.class);
    serializer.registerSerializer(11, PrimitiveObj.class);
    serializer.registerSerializer(12, WrapperObj.class);

    PrimitiveObj pri = new PrimitiveObj();
    pri.l = Long.MIN_VALUE;

    WrapperObj wrap = new WrapperObj();
    wrap.str = "Hello World!";
    wrap.l = null;

    ComposeObj composeObj = new ComposeObj();
    composeObj.pri = pri;
    composeObj.wrap = wrap;

    serializer.writeObject(buf, composeObj);

    ComposeObj res = serializer.read(buf);
    assertEquals(composeObj, res);
  }

  @Test
  void inheritanceTest() {
    serializer.registerSerializer(11, PrimitiveObj.class);
    serializer.registerSerializer(12, WrapperObj.class);
    serializer.registerSerializer(13, Child.class);

    PrimitiveObj pri = new PrimitiveObj();
    pri.l = Long.MIN_VALUE;

    WrapperObj wrap = new WrapperObj();
    wrap.str = "Hello World!";
    wrap.l = null;

    Child child = new Child();
    child.pri = pri;
    child.wrap = wrap;
    child.a = ThreadLocalRandom.current().nextInt();
    child.A = ThreadLocalRandom.current().nextInt();
    child.ignore = ThreadLocalRandom.current().nextInt() + 1;

    serializer.writeObject(buf, child);

    Child res = serializer.read(buf);
    assertEquals(child, res);
    assertNotEquals(child.ignore, res.ignore);
  }

  /**
   * 无无参构造
   *
   * @author ZJP
   * @since 2021年07月18日 13:08:10
   **/
  private static class NoDefaultConstructor {

    public NoDefaultConstructor(int i) {
    }
  }

  /**
   * 有无参构造
   *
   * @author ZJP
   * @since 2021年07月18日 13:07:59
   **/
  private static class DefaultConstructor {

    public DefaultConstructor() {
    }
  }

  /**
   * 基础类型测试类
   *
   * @author ZJP
   * @since 2021年07月18日 13:06:39
   **/
  private static class PrimitiveObj {

    public double d = Double.MAX_VALUE;
    private byte b = Byte.MAX_VALUE;
    private short s = Short.MAX_VALUE;
    private int i = Integer.MAX_VALUE;
    private long l = Long.MAX_VALUE;
    private float f = Float.MAX_VALUE;
    private char c = '中';

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PrimitiveObj that = (PrimitiveObj) o;
      return b == that.b && s == that.s && i == that.i && l == that.l
          && Float.compare(that.f, f) == 0 && Double.compare(that.d, d) == 0
          && c == that.c;
    }

    @Override
    public int hashCode() {
      return Objects.hash(b, s, i, l, f, d, c);
    }
  }


  /**
   * 包装类型测试类
   *
   * @author ZJP
   * @since 2021年07月18日 13:06:39
   **/
  private static class WrapperObj {

    public Double d = Double.MAX_VALUE;
    public String str = "hi";
    private Byte b = Byte.MAX_VALUE;
    private Short s = Short.MAX_VALUE;
    private Integer i = Integer.MAX_VALUE;
    private Long l = Long.MAX_VALUE;
    private Float f = Float.MAX_VALUE;
    private Character c = '中';

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      WrapperObj that = (WrapperObj) o;
      return Objects.equals(d, that.d) && Objects.equals(str, that.str)
          && Objects.equals(b, that.b) && Objects.equals(s, that.s)
          && Objects.equals(i, that.i) && Objects.equals(l, that.l)
          && Objects.equals(f, that.f) && Objects.equals(c, that.c);
    }

    @Override
    public int hashCode() {
      return Objects.hash(d, str, b, s, i, l, f, c);
    }
  }

  /**
   * 组合测试
   *
   * @author ZJP
   * @since 2021年07月18日 13:45:35
   **/
  private static class ComposeObj {

    public PrimitiveObj pri;
    public WrapperObj wrap;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ComposeObj that = (ComposeObj) o;
      return pri.equals(that.pri) && wrap.equals(that.wrap);
    }

    @Override
    public int hashCode() {
      return Objects.hash(pri, wrap);
    }
  }

  /**
   * 继承测试
   *
   * @author ZJP
   * @since 2021年07月18日 13:45:35
   **/
  private final static class Child extends ComposeObj {

    public int A;
    public Integer a;
    public transient int ignore;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      Child child = (Child) o;
      return A == child.A &&
          Objects.equals(a, child.a);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), A, a);
    }
  }


}
