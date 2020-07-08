package org.pj.boot;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class BootConfigTest {

  private static GenericApplicationContext context;

  @BeforeAll
  public static void init() {
    context = new AnnotationConfigApplicationContext(ServerConfig.class);
  }

  @AfterAll
  public static void close() {
    context.close();
  }

  @Test
  public void sqlTest() {
    JdbcTemplate template = context.getBean("configDao", JdbcTemplate.class);
    SqlRowSet set = template.queryForRowSet("SELECT * FROM item_item WHERE  1 = ?", 1);
    Assertions.assertTrue(set.next());
  }

  @Test
  public void mongoTest() {
    MongoTemplate template = context.getBean(MongoTemplate.class);
    System.out.println(template.collectionExists("avatar"));
  }

}
