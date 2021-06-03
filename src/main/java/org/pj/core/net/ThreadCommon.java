package org.pj.core.net;

import io.netty.channel.nio.NioEventLoopGroup;
import org.pj.core.framework.NamedThreadFactory;

/**
 * 网络公用线程组独立变量
 *
 * @author ZJP
 * @since 2020年12月09日 23:46:18
 **/
public class ThreadCommon {

  /**
   * 主线程组
   * <p>注意：主线线程组线程数不可随意添加，添加要说明用在哪。</p>
   * <p>1.接收客户端连接</p>
   */
  public static final NioEventLoopGroup BOSS = new NioEventLoopGroup(1,
      new NamedThreadFactory("BOSS"));


  /**
   * 工作线程组
   * <p>如果单服占用整台物理机则乘2否则与核心数保持一致或更少</p>
   * <p>1.客户端连接</p>
   */
  public static final NioEventLoopGroup WORKER = new NioEventLoopGroup(
      Runtime.getRuntime().availableProcessors(),
      new NamedThreadFactory("WORKER"));

}
