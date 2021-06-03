package org.pj.config;

import org.pj.common.CommonPackage;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.net.NettyTcpServer;
import org.pj.game.GamePackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/server.properties")
@ComponentScan(basePackageClasses = {GamePackage.class, CommonPackage.class})
public class GameServerConfig {

  @Bean(name = "gameContext")
  public SpringGameContext gameContext(GenericApplicationContext context,
      Environment env) {
    NettyTcpServer server = new NettyTcpServer(env.getRequiredProperty("game.port", Integer.class));
    SpringGameContext gameContext = new SpringGameContext(context);
    gameContext.setTcpServer(server);
    return gameContext;
  }
}
