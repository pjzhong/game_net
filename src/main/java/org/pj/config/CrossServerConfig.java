package org.pj.config;

import org.pj.common.CommonPackage;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.net.NettyTcpServer;
import org.pj.cross.CrossPackage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/cross.properties")
@Import(DataConfig.class)
@ComponentScan(basePackageClasses = {CrossPackage.class, CommonPackage.class})
public class CrossServerConfig {

  @Bean(name = "crossServer")
  public NettyTcpServer tcpServer(Environment env) {
    return new NettyTcpServer(env.getRequiredProperty("cross.port", Integer.class));
  }

  @Bean(name = "crossContext")
  public SpringGameContext gameContext(GenericApplicationContext context,
      @Qualifier("crossServer") NettyTcpServer tcpServer) {
    SpringGameContext gameContext = new SpringGameContext(context);
    gameContext.setTcpServer(tcpServer);
    return gameContext;
  }

}
