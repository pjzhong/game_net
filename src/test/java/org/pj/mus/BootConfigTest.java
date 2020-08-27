package org.pj.mus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/***
 *
 * @link https://docs.spring.io/spring-data/mongodb/docs/3.0.3.RELEASE/reference/html/#reference
 * */
class BootConfigTest {

  private static GenericApplicationContext context;

  private ObjectMapper mapper = new ObjectMapper();

  @BeforeAll
  static void init() {
    context = new AnnotationConfigApplicationContext(GameDataBaseConfig.class);
  }

  private String toJson(Object object) {
    try {
      return mapper.writeValueAsString( object);
    } catch (Exception ignore) {
    }
    return "";
  }

  private void initDb(MongoTemplate template) {
    for(int i : Arrays.asList(1,2,3)) {
      boolean notExists = !template.exists(new Query(where("_id").is(i)), PlayerEnt.class);
      if(notExists) {
        PlayerEnt ent = new PlayerEnt(i);
        template.insert(ent);
      }
    }
  }

  @Test
  void mongoTest() throws Exception {
    MongoTemplate template = context.getBean(MongoTemplate.class);
    initDb(template);
    searchById(template, 1);
    searchByName(template, "1");
    loadAllBaseInfo(template);
  }

  private void loadAllBaseInfo(MongoTemplate template) {
    List<BasePlayerEnt> ents =  template.findAll(BasePlayerEnt.class, "playerEnt");
    System.out.println(toJson(ents));//[{"id":1,"name":"1"},{"id":2,"name":"2"},{"id":3,"name":"3"}]
  }

  private void searchById(MongoTemplate template, int id) throws IOException {
    PlayerEnt resultEnt = template.findById(id, PlayerEnt.class);
    System.out.println(toJson(resultEnt));//{"id":1,"name":"1","oneBoxes":{"1":{"n":"TWO"}}}
  }

  private void searchByName(MongoTemplate template, String name) throws IOException {
    PlayerEnt resultEnt = template.findOne(Query.query(where("name").is(name)), PlayerEnt.class);
    System.out.println(toJson(resultEnt));//{"id":1,"name":"1","oneBoxes":{"1":{"n":"TWO"}}}
  }

}
