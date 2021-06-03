package org.pj.game.conf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明的一个 POJO 所对应的数据表名。
 *
 * @author ZJP
 * @since 2020年05月27日 10:01:03
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ConfigTable {

  /** 表名 */
  String value() default "";

}
