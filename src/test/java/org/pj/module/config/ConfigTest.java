package org.pj.module.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pj.module.conf.Config;
import org.pj.module.conf.ConfigSystem.ConfigBuilder;

public class ConfigTest {

  @Test
  public void noneNullTest() throws Exception {
    ConfigBuilder builder = new ConfigBuilder();
    Assertions.assertThrows(RuntimeException.class,
        () -> builder.autowired(new Properties(), SimpleConfig.class));
  }

  @Test
  public void simpleTest() throws Exception {
    ConfigBuilder builder = new ConfigBuilder();
    Properties properties = new Properties();
    properties.put("string", "1");
    properties.put("i", "1");
    properties.put("integer", "1");
    properties.put("dd", "1");
    properties.put("aDouble", "1");
    SimpleConfig config = builder.autowired(properties, SimpleConfig.class);

    assertNull(config.getNullString());
    assertEquals(config.getString(), "1");
    assertEquals(config.getI(), 1);
    assertEquals(config.getInteger().intValue(), 1);
    assertEquals(config.getD(), 1.0D, 0.0);
    assertEquals(config.getaDouble(), 1.0D, 0.0);
  }

  @Test
  public void parserTest() throws Exception {
    ConfigBuilder builder = new ConfigBuilder();
    Properties properties = new Properties();
    properties.put("two", "1");
    properties.put("three", "1");
    SimpleParser config = builder.autowired(properties, SimpleParser.class);

    assertEquals(config.getTwo(), 2);
    assertEquals(config.getThree(), 3);
  }

  public static class SimpleConfig {

    @Config(allowNull = true)
    private String nullString;

    @Config
    private String string;

    @Config
    private int i;

    @Config
    private Integer integer;

    @Config(alias = "dd")
    private double d;

    @Config
    private Double aDouble;

    public String getNullString() {
      return nullString;
    }

    public String getString() {
      return string;
    }

    public int getI() {
      return i;
    }

    public Integer getInteger() {
      return integer;
    }

    public double getD() {
      return d;
    }

    public Double getaDouble() {
      return aDouble;
    }
  }

  public static class SimpleParser {

    @Config(parser = "addOne", parserClass = ConfigTest.class)
    private int two;
    @Config(parser = "addTwo")
    private int three;

    public static int addTwo(String s) {
      return Integer.parseInt(s) + 2;
    }

    public int getTwo() {
      return two;
    }

    public int getThree() {
      return three;
    }
  }

  public static int addOne(String s) {
    return Integer.parseInt(s) + 1;
  }

}
