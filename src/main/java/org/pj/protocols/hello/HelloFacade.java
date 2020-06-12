package org.pj.protocols.hello;


import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.util.Objects;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.msg.Packet;
import org.pj.protocols.Facade;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;

/**
 * Hello World
 *
 * @author ZJP
 * @since 2020年06月10日 15:53:58
 **/
@Facade
public class HelloFacade {


  @Packet(1)
  public Message HelloWorld() {
    return Message.newBuilder().setBody(ByteString.copyFromUtf8("HelloWorld")).build();
  }


  @Packet(2)
  public Message echoContext(Channel channel, Message message) {
    Objects.requireNonNull(channel);
    return message;
  }

  @Packet(3)
  public HelloWorld echoHelloWorld(Channel channel, HelloWorld world) {
    Objects.requireNonNull(channel);
    return world;
  }

  public HelloWorld noEffect(HelloWorld world) {
    return world;
  }
}
