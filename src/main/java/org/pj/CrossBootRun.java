package org.pj;

import java.util.Collections;
import java.util.List;
import org.pj.config.CrossServerConfig;
import org.pj.config.DataConfig;
import org.pj.cross.CrossPackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import({CrossServerConfig.class, DataConfig.class, MainRun.class})
@ComponentScan("none")
public class CrossBootRun {

  public static void main(String[] args) {
    List<Class<?>> classList = Collections.singletonList(CrossBootRun.class);
    SpringApplication app = new SpringApplication(classList.toArray(new Class[0]));
    app.run(args);
  }

}
