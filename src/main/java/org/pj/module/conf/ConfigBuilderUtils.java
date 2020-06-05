package org.pj.module.conf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.commons.lang3.StringUtils;

/**
 * 配置构建工具类
 *
 * @author ZJP
 * @since 2020年05月27日 11:25:42
 **/
public class ConfigBuilderUtils {

  /**
   * 使用 {@code properties} 的来填充配置
   *
   * @author ZJP
   * @since 2020年05月27日 11:08:07
   **/
  public static <T> T autowired(Properties properties, Class<T> clazz) throws Exception {
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
  public static <T> T autowired(Properties properties, Class<T> clazz, Collection<Field> fields)
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

  public static <T> void setValue(Properties properties, Class<T> clazz, T instance,
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
    }

    Class<?> type = ClassUtils.primitiveToWrapper(field.getType());

    if (!"".equals(conf.parser())) {
      Class<?> parseClass =
          conf.parserClass() == Null.class ? instance.getClass() : conf.parserClass();
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

  private static Object parserMethod(Object instance, String parser, Class<?> parseClass,
      String propValue) throws Exception {
    Method method = parseClass.getDeclaredMethod(parser, String.class);
    method.setAccessible(true);
    Object result = null;
    if (instance.getClass() == parseClass) {
      result = method.invoke(instance, propValue);
    } else {
      result = method.invoke(parseClass, propValue);
    }

    return result;
  }

}
