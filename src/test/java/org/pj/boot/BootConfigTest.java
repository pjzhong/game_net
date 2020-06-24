package org.pj.boot;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class BootConfigTest {

  private static GenericApplicationContext context;

  @BeforeClass
  public static void init() {
    context = new AnnotationConfigApplicationContext(ServerConfig.class);
  }

  @AfterClass
  public static void close() {
    context.close();
  }

  @Test
  public void sqlTest() {
    JdbcTemplate template = context.getBean("configDao", JdbcTemplate.class);
    SqlRowSet set = template.queryForRowSet("SELECT * FROM item_item WHERE  1 = ?", 1);
    Assert.assertTrue(set.next());
  }

  @Test
  public void mongoTest() {
    MongoTemplate template = context.getBean(MongoTemplate.class);
    System.out.println(template.collectionExists("avatar"));
  }

}
