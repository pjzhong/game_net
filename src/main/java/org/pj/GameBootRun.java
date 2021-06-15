package org.pj;

import java.util.ArrayList;
import java.util.List;
import org.pj.config.CrossServerConfig;
import org.pj.config.DataConfig;
import org.pj.config.GameServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = {"none"})
public class GameBootRun {

  public static void main(String[] args) {
    String model = args[0];
    List<Class<?>> configClass = new ArrayList<>();
    configClass.add(GameBootRun.class);
    configClass.add(MainRun.class);
    configClass.add(DataConfig.class);
    if (model.equals("cross")) {
      configClass.add(CrossServerConfig.class);
    } else {
      configClass.add(GameServerConfig.class);
    }
    SpringApplication app = new SpringApplication(configClass.toArray(new Class[0]));
    app.run(args);
  }

}
