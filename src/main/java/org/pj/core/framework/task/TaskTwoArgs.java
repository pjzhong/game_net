package org.pj.core.framework.task;

/**
 * A task with Three Arg
 *
 * @author ZJP
 * @since 2021年05月16日 17:52:05
 **/
@FunctionalInterface
public interface TaskTwoArgs<A, B>  {

  void run(A a, B b);
}
