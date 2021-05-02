package org.pj.core.msg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

public class ProtoTest {

  @Test
  public void messageProtoTest() {
    Message message = Message.valueOf()
        .setModule(1)
        .setStates(200)
        .setOpt(1)
        .setBody(ByteString.copyFromUtf8("Hello World!!!!!!!!!").toByteArray());

    byte[] array = message.toByteArray();
    Message parseMessage = Message.readFrom(ByteBuffer.wrap(array));
    assertEquals(message, parseMessage);
  }

}
