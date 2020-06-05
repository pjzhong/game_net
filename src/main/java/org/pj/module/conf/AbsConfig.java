package org.pj.module.conf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * 通用配置解析逻辑
 *
 * @author ZJP
 * @since 2020年06月05日 16:06:51
 **/
public abstract class AbsConfig {

  protected JdbcTemplate template;

  public void load(ApplicationContext context) throws Exception {
    template = context.getBean("configDao", JdbcTemplate.class);
    setValue();
  }

  public <T> T loadInit(Class<T> clazz) throws Exception {
    String tName = requiredTableName(clazz);
    SqlRowSet set = template.queryForRowSet("SELECT * FROM " + tName);
    Properties properties = new Properties();
    while (set.next()) {
      properties.put(set.getString("key"), set.getString("value"));
    }
    return ConfigBuilderUtils.autowired(properties, clazz);
  }

  /**
   * 加载配置
   *
   * @param keyMethod 主键获取
   * @param clazz 配置实体类
   * @since 2020年06月05日 18:00:12
   */
  public <K, V> Map<K, V> loadValuesMap(Function<V, K> keyMethod, Class<V> clazz) throws Exception {
    Map<K, V> result = new HashMap<>();
    for (V val : loadValues(clazz)) {
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

      T t = ConfigBuilderUtils.autowired(properties, clazz, fieldSet);
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
    String prefix = prefix();
    return StringUtils.isNotBlank(prefix) ? prefix() + "_" + table.value() : table.value();
  }

  public abstract void setValue() throws Exception;


  public String prefix() {
    return "";
  }


}
