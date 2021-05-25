package org.pj.module.util.task;

/**
 * A task with Three Arg
 *
 * @author ZJP
 * @since 2021年05月16日 17:52:05
 **/
@FunctionalInterface
public interface TaskVarArgs  {

  void run(Object... args);
}
