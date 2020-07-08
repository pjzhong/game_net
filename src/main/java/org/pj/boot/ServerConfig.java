package org.pj.boot;

import org.pj.core.net.NettyTcpServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/server.properties")
@Import(GameDataBaseConfig.class)
public class ServerConfig {

  @Bean
  public NettyTcpServer tcpServer(Environment env) {
    return new NettyTcpServer(env.getRequiredProperty("game.port", Integer.class));
  }
}
