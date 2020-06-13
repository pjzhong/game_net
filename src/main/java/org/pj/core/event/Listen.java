package org.pj.core.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Listen {

  /**
   * 时间编号
   *
   * @author ZJP
   * @since 2020年06月13日 18:35:00
   **/
  int value();
}
