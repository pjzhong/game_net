package org.pj.mus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@PropertySource("classpath:/server.properties")
public class GameDataBaseConfig {


    @Bean(name = "game_mongo", destroyMethod = "closeClient")
    public SimpleMongoClientDatabaseFactory simpleMongoClientDatabaseFactory(Environment env) {
        return new SimpleMongoClientDatabaseFactory(env.getRequiredProperty("mongo.url"));
    }

    @Bean
    public MongoTemplate mongoTemplate(@Qualifier("game_mongo") MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }

}
