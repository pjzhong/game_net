package org.pj.msg;


import io.netty.channel.Channel;
import org.pj.msg.MessageProto.Message;

/**
 * 调用环境
 *
 * @author ZJP
 * @since 2020年06月10日 17:01:55
 **/
public class InvokeContext {

  /** 连接 */
  private final Channel channel;
  /** 请求的消息 */
  private final Message message;

  public InvokeContext(Channel channel, Message message) {
    this.channel = channel;
    this.message = message;
  }

  public Channel getChannel() {
    return channel;
  }

  public Message getMessage() {
    return message;
  }

}
