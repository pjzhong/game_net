package org.pj.boot;

import com.mongodb.ConnectionString;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.pj.core.event.EventBus;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.msg.MessageDispatcher;
import org.pj.core.net.TcpServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@PropertySource("classpath:/server.properties")
@ComponentScan(value = {"org.pj.module", "org.pj.protocols"})
public class ServerConfig {

  @Bean
  public TcpServer tcpServer(Environment env) {
    return new TcpServer(env.getRequiredProperty("game.port", Integer.class));
  }

  @Bean(destroyMethod = "close")
  public SpringGameContext gameContext(GenericApplicationContext context, TcpServer tcpServer) {
    SpringGameContext gameContext = new SpringGameContext(context);
    gameContext.setEventBus(new EventBus());
    gameContext.setTcpServer(tcpServer);
    gameContext
        .setDispatcher(new MessageDispatcher(Runtime.getRuntime().availableProcessors() * 2));
    gameContext.init();
    return gameContext;
  }

  /* 数据库配置 */
  @Bean(value = "configDataSource", destroyMethod = "close")
  public HikariDataSource configDataSource(Environment env) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(env.getRequiredProperty("db.config.url"));
    config.setDriverClassName("com.mysql.jdbc.Driver");
    config.setMaximumPoolSize(50);
    config.setMinimumIdle(0);
    config.setConnectionTestQuery("select 1");
    config.setPoolName("configDataSource");
    return new HikariDataSource(config);
  }

  @Bean("configDao")
  public JdbcTemplate configJdbcTemplate(@Qualifier("configDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean(destroyMethod = "closeClient")
  public SimpleMongoClientDbFactory simpleMongoClientDatabaseFactory(Environment env) {
    return new SimpleMongoClientDbFactory(
        new ConnectionString(env.getRequiredProperty("mongo.url")));
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoDbFactory factory) {
    return new MongoTemplate(factory);
  }

}
