package org.pj.msg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Assert;
import org.junit.Test;
import org.pj.msg.MessageProto.Message;
import org.pj.msg.adp.ContextAdapter;
import org.pj.msg.adp.ProtobufAdapter;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

public class IAdapterTest {

  @Test
  public void contextFieldTest() {
    ContextAdapter adapter = ContextAdapter.getInstance();

    assertFalse(adapter.isContextField(String.class));

    assertTrue(adapter.isContextField(Message.class));
    assertTrue(adapter.isContextField(Channel.class));
    assertTrue(adapter.isContextField(NioSocketChannel.class));
  }


  @Test
  public void protobufAdapterTest() throws InvalidProtocolBufferException {
    ProtobufAdapter adapter = ProtobufAdapter.getInstance();

    Assert.assertNull(adapter.extractParser(Integer.class));
    Assert.assertNotNull(adapter.extractParser(Message.class));
    Assert.assertNotNull(adapter.extractParser(HelloWorld.class));

    Parser<?> parser = adapter.extractParser(Message.class);
    Parser<?> helloWorldParser = adapter.extractParser(HelloWorld.class);

    HelloWorld helloWorld = HelloWorld.newBuilder().setStr("EchoHelloWorld").build();
    Message message = Message.newBuilder().setBody(helloWorld.toByteString()).build();
    Message echoMessage = (Message) parser.parseFrom(message.toByteString());
    HelloWorld echoHelloWorld = (HelloWorld) helloWorldParser.parseFrom(echoMessage.getBody());

    Assert.assertEquals(message, echoMessage);
    Assert.assertEquals(helloWorld, echoHelloWorld);
  }

}
