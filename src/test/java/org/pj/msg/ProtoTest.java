package org.pj.msg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.pj.msg.MessageProto.Message;

public class ProtoTest {


  @Test
  public void messageProtoTest() throws InvalidProtocolBufferException {
    Message message = Message.newBuilder()
        .setVersion(1)
        .setModule(1)
        .setStat(200)
        .setSerial(1)
        .setBody(ByteString.copyFromUtf8("Hello World!!!!!!!!!"))
        .build();

    byte[] array = message.toByteArray();
    System.out.println(Arrays.toString(array));
    Message parseMessage = Message.parseFrom(array);
    Assert.assertEquals(message, parseMessage);
  }

}
