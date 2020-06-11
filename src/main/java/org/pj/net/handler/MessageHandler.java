package org.pj.net.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.pj.msg.MessageDispatcher;
import org.pj.msg.MessageProto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息分发
 *
 * @author ZJP
 * @since 2020年06月11日 14:01:00
 **/
@Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

  private Logger log = LoggerFactory.getLogger(MessageHandler.class);
  private final MessageDispatcher dispatcher;

  public MessageHandler(MessageDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelActive();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelInactive();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
    dispatcher.add(ctx.channel(), msg);
  }


  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  /**
   * Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward to the next
   * {@link ChannelHandler} in the {@link ChannelPipeline}.
   *
   * Sub-classes may override this method to change behavior.
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {
    this.log.error("UnExpected Exception", cause);
  }


}
