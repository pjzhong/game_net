package org.pj.core.serde;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArraySerializerTest {

  private CommonSerializer serializer;
  private ByteBuf buf;

  @BeforeEach
  private void beforeEach() {
    serializer = new CommonSerializer();
    buf = Unpooled.buffer();
  }

  @Test
  void oneDimensionsCheckTest() {
    int[] array = new int[]{};
    assertArrayEquals(new int[]{0}, ArraySerializer.getDimensions(array));
    int[] arrayTwo = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    assertArrayEquals(new int[]{10}, ArraySerializer.getDimensions(arrayTwo));
  }

  @Test
  void fourDimensionsCheckTest() {
    int[][][][] array = new int[3][2][4][6];
    assertArrayEquals(new int[]{3, 2, 4, 6}, ArraySerializer.getDimensions(array));
  }

  @Test
  void diffLengthDimensionCheckTest() {
    int[][][][] array = new int[2][2][4][6];
    array[0] = new int[10][11][12];
    assertThrows(RuntimeException.class, () -> ArraySerializer.getDimensions(array));
  }

  @Test
  void emptyArrayTest() {
    int[] test = new int[]{};
    serializer.writeObject(buf, test);

    int[] res = serializer.read(buf);
    assertArrayEquals(test, res);
  }

  @Test
  void simpleArrayTest() {
    int[] test = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    serializer.writeObject(buf, test);

    int[] res = serializer.read(buf);
    assertArrayEquals(test, res);
  }

  @Test
  void twoDimensionIntArrayTest() {
    int[][] test = new int[][]{{1, 10}, {10, 1}};
    serializer.writeObject(buf, test);
    int[][] res = serializer.read(buf);
    assertArrayEquals(test, res);
  }

  @Test
  void twoDimensionLongArrayTest() {
    long[][] test = new long[][]{{Long.MAX_VALUE, Long.MIN_VALUE}, {10, 1}};
    serializer.writeObject(buf, test);
    long[][] res = serializer.read(buf);
    assertArrayEquals(test, res);
  }

}
