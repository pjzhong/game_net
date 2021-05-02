package org.pj.boot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.pj.core.event.EventBus;
import org.pj.core.framework.SpringGameContext;
import org.pj.core.net.NettyTcpServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.jdbc.core.JdbcTemplate;


public class GameDataBaseConfig {


  @Bean
  public SpringGameContext gameContext(GenericApplicationContext context,
      NettyTcpServer tcpServer) {
    SpringGameContext gameContext = new SpringGameContext(context);
    gameContext.setEventBus(new EventBus());
    gameContext.setTcpServer(tcpServer);
    return gameContext;
  }

  /* 数据库配置 */
 /* @Bean("configDao")
  public JdbcTemplate configJdbcTemplate(@Qualifier("configDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean(name = "game_mongo", destroyMethod = "closeClient")
  public SimpleMongoClientDatabaseFactory simpleMongoClientDatabaseFactory(Environment env) {
    return new SimpleMongoClientDatabaseFactory(env.getRequiredProperty("mongo.url"));
  }

  @Bean
  public MongoTemplate mongoTemplate(@Qualifier("game_mongo") MongoDatabaseFactory factory) {
    return new MongoTemplate(factory);
  }

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
  }*/

}
