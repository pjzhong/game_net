package org.pj.core.framework.task;

/**
 * A task with Two Arg
 *
 * @author ZJP
 * @since 2021年05月16日 17:52:05
 **/
@FunctionalInterface
public interface TaskThreeArgs<A, B, C>  {

  void run(A a, B b, C c);
}
