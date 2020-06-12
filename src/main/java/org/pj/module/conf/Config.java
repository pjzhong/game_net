package org.pj.module.conf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置文件中key---value的注解，用于读取key 的value的值 alias :字段名称 parser :解析方法名称 <br> allowNull是否允许为null
 * ,getKey存放在map时需要存放的的key值
 *
 * @data 2014年10月15日 下午2:10:06
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

  /** 字段别名 */
  String alias() default "";

  /**
   * 内容解析器
   *
   * 如果有内容解析器，allowNull将失效，请自行处理空值情况
   *
   * @since 2020年06月12日 14:58:48
   */
  String parser() default "";

  /** 内容解析类 */
  Class<?> parserClass() default Object.class;

  /** 能否为空 */
  boolean allowNull() default false;

}
