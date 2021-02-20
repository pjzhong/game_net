package org.pj.protocols.hello;


import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.msg.Packet;
import org.pj.protocols.Facade;
import org.pj.protocols.hello.HelloWorldProto.HelloWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello World
 *
 * @author ZJP
 * @since 2020年06月10日 15:53:58
 **/
@Facade
public class HelloFacade {


  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private AtomicInteger integer = new AtomicInteger();

  @Packet(1)
  public Message echoHelloWorld() {
    return Message.newBuilder().setBody(ByteString.copyFromUtf8("HelloWorld")).build();
  }

  @Packet(2)
  public Message echoContext(Channel channel, Message message) {
    Objects.requireNonNull(channel);
    return message;
  }

  @Packet(3)
  public HelloWorld echoHelloWorld(HelloWorld world) {
    logger.info(world.getStr());
    return world;
  }

  @Packet(4)
  public HelloWorld count(Channel channel) {
    HelloWorld h = HelloWorld.newBuilder().setCount(integer.incrementAndGet()).build();
    String address = channel.remoteAddress().toString();

    logger.info("from {} count {}", address.substring(address.lastIndexOf(':')), h.getCount());
    return h;
  }

  @Packet(5)
  public Message throwException() {
    throw new UnsupportedOperationException();
  }

  public HelloWorld noEffect(HelloWorld world) {
    return world;
  }
}
