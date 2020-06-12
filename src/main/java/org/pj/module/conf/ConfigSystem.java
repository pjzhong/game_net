package org.pj.module.conf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class ConfigSystem {

  @Autowired
  @Qualifier("configDao")
  private JdbcTemplate jdbcTemplate;


  public ConfigBuilder builder() {
    return new ConfigBuilder(jdbcTemplate);
  }

  public ConfigBuilder builderWithPrefix(String prefix) {
    return new ConfigBuilder(jdbcTemplate, prefix);
  }


  public static class ConfigBuilder {

    private JdbcTemplate template;
    private String prefix;
    private Map<String, Method> parsers;

    public ConfigBuilder() {
      parsers = new HashMap<>();
    }

    public ConfigBuilder(JdbcTemplate template) {
      this();
      this.template = template;
      this.prefix = null;
    }

    public ConfigBuilder(JdbcTemplate template, String prefix) {
      this();
      this.template = template;
      this.prefix = prefix;
    }

    public JdbcTemplate getTemplate() {
      return template;
    }

    public <T> T loadInit(Class<T> clazz) throws Exception {
      String tName = requiredTableName(clazz);
      SqlRowSet set = template.queryForRowSet("SELECT * FROM " + tName);
      Properties properties = new Properties();
      while (set.next()) {
        properties.put(set.getString("key"), set.getString("value"));
      }
      return autowired(properties, clazz);
    }

    /**
     * 加载配置
     *
     * @param keyMethod 主键获取
     * @param clazz 配置实体类
     * @since 2020年06月05日 18:00:12
     */
    public <K, V> Map<K, V> loadValuesMap(Function<V, K> keyMethod, Class<V> clazz)
        throws Exception {
      Map<K, V> result = new HashMap<>();
      for (V val : loadValues(clazz)) {
        result.put(keyMethod.apply(val), val);
      }
      return Collections.unmodifiableMap(result);
    }

    /**
     * 加载配置
     *
     * @param keyMethod 主键获取
     * @param clazz 配置实体类
     * @since 2020年06月05日 18:00:12
     */
    public <K, V> Map<K, V> loadValuesMap(String tableName, Function<V, K> keyMethod,
        Class<V> clazz)
        throws Exception {
      Map<K, V> result = new HashMap<>();
      for (V val : loadValues(tableName, clazz)) {
        result.put(keyMethod.apply(val), val);
      }
      return Collections.unmodifiableMap(result);
    }

    /**
     * 加载配置
     *
     * @param clazz 配置实体类
     * @since 2020年06月05日 17:59:20
     */
    public <T> List<T> loadValues(Class<T> clazz) throws Exception {
      return loadValues(requiredTableName(clazz), clazz);
    }

    /**
     * 加载配置
     *
     * @param tableName 配表名
     * @param clazz 配置实体类
     * @since 2020年06月05日 17:59:32
     */
    public <T> List<T> loadValues(String tableName, Class<T> clazz) throws Exception {
      SqlRowSet set = template.queryForRowSet("SELECT * FROM " + tableName);

      List<T> results = new ArrayList<>();
      Properties properties = new Properties();

      Set<Field> fieldSet = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
      fieldSet.addAll(Arrays.asList(clazz.getFields()));

      while (set.next()) {
        properties.clear();
        String[] colNames = set.getMetaData().getColumnNames();
        for (String n : colNames) {
          String value = set.getString(n);
          properties.put(n, value == null ? "" : value);
        }

        T t = autowired(properties, clazz, fieldSet);
        results.add(t);
      }
      return Collections.unmodifiableList(results);
    }

    private String requiredTableName(Class<?> clazz) {
      ConfigTable table = clazz.getAnnotation(ConfigTable.class);
      if (ObjectUtils.isEmpty(table) || StringUtils.isBlank(table.value())) {
        throw new IllegalArgumentException(
            String.format("clazz:%s, table name is null", clazz.getName()));
      }
      return StringUtils.isNotBlank(prefix) ? prefix + "_" + table.value() : table.value();
    }


    /**
     * 使用 {@code properties} 的来填充配置
     *
     * @author ZJP
     * @since 2020年05月27日 11:08:07
     **/
    public <T> T autowired(Properties properties, Class<T> clazz) throws Exception {
      Field[] allFields = clazz.getDeclaredFields();
      Field[] allPubFields = clazz.getFields();

      Set<Field> fieldSet = new HashSet<>();
      fieldSet.addAll(Arrays.asList(allFields));
      fieldSet.addAll(Arrays.asList(allPubFields));

      return autowired(properties, clazz, fieldSet);
    }

    /**
     * 使用 {@code properties} 的来填充配置
     *
     * @param fields 填充的字段
     * @author ZJP
     * @since 2020年05月27日 11:08:07
     **/
    public <T> T autowired(Properties properties, Class<T> clazz, Collection<Field> fields)
        throws Exception {
      T instance = clazz.getDeclaredConstructor().newInstance();
      for (Field f : fields) {
        f.setAccessible(true);
        Config conf = f.getAnnotation(Config.class);
        if (conf != null) {
          try {
            setValue(properties, clazz, instance, f, conf);
          } catch (Exception e) {
            throw new RuntimeException(String
                .format("字段:%s.%s,数值:%s, 错误:%s", clazz.getName(), f.getName(),
                    properties.get(f.getName()), e.getMessage()), e);
          }
        }
      }

      return instance;
    }

    public <T> void setValue(Properties properties, Class<T> clazz, T instance,
        Field field, Config conf) throws Exception {

      String alias = conf.alias();
      String finalName = StringUtils.isBlank(alias) ? field.getName() : alias;

      String propValue = properties.getProperty(finalName);
      if (propValue == null) {
        propValue = properties.getProperty(finalName.toLowerCase());
      }
      if (StringUtils.isBlank(propValue)) {
        if (!conf.allowNull() && StringUtils.isBlank(conf.parser())) {
          throw new RuntimeException(
              String.format("class:%s.%s Required none null", clazz.getName(), field
                  .getName()));
        }
        if (StringUtils.isBlank(conf.parser())) {
          return;
        }
      }

      Class<?> type = ClassUtils.primitiveToWrapper(field.getType());

      if (!"".equals(conf.parser())) {
        Class<?> parseClass =
            conf.parserClass() == Object.class ? instance.getClass() : conf.parserClass();
        field.set(instance, parserMethod(instance, conf.parser(), parseClass, propValue));
      } else if (type == Integer.class) {
        field.set(instance, Integer.parseInt(propValue));
      } else if (type == Long.class) {
        field.set(instance, Long.parseLong(propValue));
      } else if (type == Boolean.class) {
        if (StringUtils.equals(propValue, "1")) {
          field.set(instance, true);
        } else if (StringUtils.equals(propValue, "0")) {
          field.set(instance, false);
        } else {
          field.set(instance, Boolean.parseBoolean(propValue));
        }
      } else if (type == Byte.class) {
        field.set(instance, Byte.parseByte(propValue));
      } else if (type == Double.class) {
        field.set(instance, Double.parseDouble(propValue));
      } else if (type == Short.class) {
        field.set(instance, Short.parseShort(propValue));
      } else if (type == Float.class) {
        field.set(instance, Float.parseFloat(propValue));
      } else if (type == String.class) {
        field.set(instance, propValue);
      } else {
        throw new RuntimeException(
            clazz.getName() + "." + field.getName() + "非基本类型，请指定解析方法.");
      }
    }

    private Object parserMethod(Object instance, String parser, Class<?> parseClass,
        String propValue) throws Exception {
      Method method = extractParserMethod(parser, parseClass);
      method.setAccessible(true);
      Object result = null;
      if (instance.getClass() == parseClass) {
        result = method.invoke(instance, propValue);
      } else {
        result = method.invoke(parseClass, propValue);
      }

      return result;
    }

    private Method extractParserMethod(String parser, Class<?> parserClass)
        throws NoSuchMethodException {
      String key = parserClass.getName() + "." + parser;
      Method m = parsers.get(key);
      if (m == null) {
        m = parserClass.getDeclaredMethod(parser, String.class);
        parsers.put(key, m);
      }
      return m;
    }

  }
}
