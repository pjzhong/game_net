package org.pj.boot;

import com.mongodb.ConnectionString;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@PropertySource("classpath:/server.properties")
public class AppConfig {

  @Bean(value = "configDataSource", destroyMethod = "close")
  public HikariDataSource configDataSource(Environment env) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(env.getRequiredProperty("db.config.url"));
    config.setDriverClassName("com.mysql.jdbc.Driver");
    config.setMaximumPoolSize(50);
    config.setMinimumIdle(1);
    config.setConnectionInitSql("select 1");
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
