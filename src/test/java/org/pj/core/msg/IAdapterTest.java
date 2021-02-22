package org.pj.core.msg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import io.netty.channel.Channel;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;
import org.pj.core.msg.adp.ContextAdapter;
import org.pj.core.msg.adp.ProtobufAdapter;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

public class IAdapterTest {

  @Test
  public void contextFieldTest() {
    ContextAdapter adapter = ContextAdapter.getInstance();

    assertFalse(adapter.isContextField(String.class));

    assertTrue(adapter.isContextField(Message.class));
    assertTrue(adapter.isContextField(Channel.class));
    assertTrue(adapter.isContextField(Channel.class));
  }


  @Test
  public void protobufAdapterTest() throws InvalidProtocolBufferException {
    ProtobufAdapter adapter = ProtobufAdapter.getInstance();

    assertNull(adapter.extractParser(Integer.class));
    assertNull(adapter.extractParser(Message.class));
    assertNotNull(adapter.extractParser(HelloWorld.class));

    Parser<?> helloWorldParser = adapter.extractParser(HelloWorld.class);

    HelloWorld helloWorld = HelloWorld.newBuilder().setStr("EchoHelloWorld").build();
    Message message = Message.valueOf().setBody(helloWorld.toByteArray());
    Message echoMessage = Message.readFrom(ByteBuffer.wrap(message.toByteArray()));
    HelloWorld echoHelloWorld = (HelloWorld) helloWorldParser.parseFrom(echoMessage.getBody());

    assertEquals(message, echoMessage);
    assertEquals(helloWorld, echoHelloWorld);
  }

}
