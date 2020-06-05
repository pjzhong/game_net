package org.pj.boot;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class BootConfigTest {

  @Test
  public void test() {
    ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

    JdbcTemplate template = context.getBean("configDao", JdbcTemplate.class);
    SqlRowSet set = template.queryForRowSet("SELECT * FROM item_item WHERE  1 = ?", 1);
    while(set.next()) {
      for(String s : set.getMetaData().getColumnNames()) {
        System.out.format("%s-%s ", s, set.getString(s));
      }
      System.out.println();
    }
  }

}
