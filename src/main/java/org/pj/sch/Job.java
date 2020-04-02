package org.pj.sch;

/**
 * @author zhongjp
 * @since 2018年07月23日 10:17
 */
@FunctionalInterface
public interface Job {

  void execute(Trigger t);
}
