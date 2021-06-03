package org.pj.game.util.task.wrapper;

import org.pj.game.util.task.TaskFourArgs;
import org.pj.game.util.task.TaskOneArg;
import org.pj.game.util.task.TaskThreeArgs;
import org.pj.game.util.task.TaskTwoArgs;
import org.pj.game.util.task.TaskVarArgs;

/**
 * 把TaskXX包装成RunAble
 *
 * @author ZJP
 * @since 2021年05月16日 18:54:00
 **/
public class TaskWrapper {


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

  public static <A, B, C, D> Runnable of(TaskFourArgs<A, B, C, D> task, A a, B b, C c, D d) {
    return GameTaskFourArg.of(task, a, b, c, d);
  }

  public static Runnable ofArgs(TaskVarArgs task, Object... args) {
    return GameTaskVarArg.of(task, args);
  }

}
