package org.pj;

import java.util.Arrays;
import java.util.List;
import org.pj.config.CrossServerConfig;
import org.pj.config.DataBaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX,
    pattern = "org.pj.config.*"))
public class CrossBootRun {

  public static void main(String[] args) {
    List<Class<?>> classList = Arrays.asList(CrossBootRun.class, CrossServerConfig.class,
        DataBaseConfig.class);
    SpringApplication app = new SpringApplication(classList.toArray(new Class[0]));
    app.run(args);
  }

}
