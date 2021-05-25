package org.pj.module.util.task;

/**
 * A task with Four Arg
 *
 * @author ZJP
 * @since 2021年05月16日 17:52:05
 **/
@FunctionalInterface
public interface TaskFourArgs<A, B, C,D>  {

  void run(A a, B b, C c, D d);
}
