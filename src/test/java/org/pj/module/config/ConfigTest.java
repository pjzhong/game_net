package org.pj.module.config;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;
import org.pj.module.conf.Config;
import org.pj.module.conf.ConfigBuilderUtils;

public class ConfigTest {

  @Test(expected = RuntimeException.class)
  public void noneNullTest() throws Exception {
    ConfigBuilderUtils.autowired(new Properties(), SimpleConfig.class);
  }

  @Test
  public void simpleTest() throws Exception {
    Properties properties = new Properties();
    properties.put("string", "1");
    properties.put("i", "1");
    properties.put("integer", "1");
    properties.put("dd", "1");
    properties.put("aDouble", "1");
    SimpleConfig config = ConfigBuilderUtils.autowired(properties, SimpleConfig.class);

    Assert.assertNull(config.getNullString());
    Assert.assertEquals(config.getString(), "1");
    Assert.assertEquals(config.getI(), 1);
    Assert.assertEquals(config.getInteger().intValue(), 1);
    Assert.assertEquals(config.getD(), 1.0D, 0.0);
    Assert.assertEquals(config.getaDouble(), 1.0D, 0.0);
  }

  @Test
  public void parserTest() throws Exception {
    Properties properties = new Properties();
    properties.put("two", "1");
    properties.put("three", "1");
    SimpleParser config = ConfigBuilderUtils.autowired(properties, SimpleParser.class);

    Assert.assertEquals(config.getTwo(), 2);
    Assert.assertEquals(config.getThree(), 3);
  }

  @Test
  public void avaConfigTest() {

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
