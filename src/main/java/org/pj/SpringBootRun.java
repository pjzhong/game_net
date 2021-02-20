package org.pj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.pj.boot.CrossServerConfig;
import org.pj.boot.ServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX,
    pattern = "org.pj.boot.*"))
public class SpringBootRun {

  public static void main(String[] args) {
    boolean cross = Arrays.asList(args).contains("cross");
    List<Class> classList = new ArrayList<>();
    classList.add(SpringBootRun.class);
    if (cross) {
      classList.add(CrossServerConfig.class);
    } else {
      classList.add(ServerConfig.class);
    }

    SpringApplication app = new SpringApplication(classList.toArray(new Class[0]));
    app.run(args);
  }

}
