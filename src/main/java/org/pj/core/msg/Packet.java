package org.pj.core.msg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ZJP
 * @since 2020年06月10日 14:28:19
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Packet {

  /**
   * 协议号
   *
   * @since 2020年06月10日 14:31:04
   */
  int value();
}
