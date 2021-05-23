package org.pj.core.framework.task;

/**
 * A task with One Arg
 *
 * @author ZJP
 * @since 2021年05月16日 17:52:05
 **/
@FunctionalInterface
public interface TaskOneArg<T>  {

  void run(T arg);
}
