package org.pj.boot;

import com.mongodb.ConnectionString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;

@Configuration
@PropertySource("classpath:/server.properties")
public class AppConfig {

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
