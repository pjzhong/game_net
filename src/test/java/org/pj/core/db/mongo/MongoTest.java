package org.pj.core.db.mongo;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.pj.config.DataBaseConfig;
import org.pj.config.ServerConfig;
import org.pj.module.avatar.dao.Avatar;
import org.pj.module.avatar.dao.AvatarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@EnabledIfSystemProperty(named = "mongo", matches = "true")
@SpringBootTest(classes = {ServerConfig.class, DataBaseConfig.class})
public class MongoTest {

  @Autowired
  private MongoTemplate template;
  @Autowired
  private AvatarRepository repository;


  @AfterEach
  public  void close() {
    template.dropCollection(Avatar.class);
  }

  @Test
  public void mongoTest() {
    Avatar avatar = new Avatar();
    avatar.setId(1);
    template.save(avatar);

    Avatar db = template.findOne(Query.query(Criteria.where("_id").is(1)), Avatar.class);

    Assertions.assertNotNull(db);
    Assertions.assertEquals(avatar.getId(), db.getId());
  }

  @Test
  public void mongoRepositoryTest() {
    Avatar avatar = new Avatar();
    avatar.setId(2);
    repository.save(avatar);

    Optional<Avatar> db = repository.findById(2L);

    Assertions.assertTrue(db.isPresent());
    Assertions.assertEquals(avatar.getId(), db.get().getId());
  }

}
