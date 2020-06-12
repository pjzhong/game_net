package org.pj.protocols.hello;


import io.netty.channel.Channel;
import org.pj.core.msg.Packet;
import org.pj.protocols.Facade;

/**
 * Hello World
 *
 * @author ZJP
 * @since 2020年06月10日 15:53:58
 **/
@Facade
public class HelloFacade {

  @Packet(1)
  public HelloWorldProto HelloWorld(Channel channel, HelloWorldProto message) {
    return message;
  }
}
