package org.pj.config;

import org.pj.common.CommonPackage;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.net.NettyTcpServer;
import org.pj.game.GamePackage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/server.properties")
@ComponentScan(basePackageClasses = {GamePackage.class, CommonPackage.class})
public class ServerConfig {

  @Bean(name = "gameContext")
  public SpringGameContext gameContext(GenericApplicationContext context,
      @Qualifier("gameServer") NettyTcpServer tcpServer) {
    SpringGameContext gameContext = new SpringGameContext(context);
    gameContext.setTcpServer(tcpServer);
    return gameContext;
  }

  @Bean(name = "gameServer")
  public NettyTcpServer tcpServer(Environment env) {
    return new NettyTcpServer(env.getRequiredProperty("game.port", Integer.class));
  }
}
