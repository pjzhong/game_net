package org.pj.core.framework.task.wrapper;

import org.pj.core.framework.task.TaskOneArg;
import org.pj.core.framework.task.TaskThreeArgs;
import org.pj.core.framework.task.TaskTwoArgs;
import org.pj.core.framework.task.TaskVarArgs;

/**
 * 把TaskXX包装成RunAble
 *
 * @author ZJP
 * @since 2021年05月16日 18:54:00
 **/
public class GameTaskWrapper {


  public static Runnable of(Runnable run) {
    return run;
  }

  public static <A> Runnable of(TaskOneArg<A> task, A a) {
    return GameTaskOneArg.of(task, a);
  }

  public static <A, B> Runnable of(TaskTwoArgs<A, B> task, A a, B b) {
    return GameTaskTwoArg.of(task, a, b);
  }

  public static <A, B, C> Runnable of(TaskThreeArgs<A, B, C> task, A a, B b, C c) {
    return GameTaskThreeArg.of(task, a, b, c);
  }

  public static Runnable ofArgs(TaskVarArgs task, Object... args) {
    return GameTaskVarArg.of(task, args);
  }

}
