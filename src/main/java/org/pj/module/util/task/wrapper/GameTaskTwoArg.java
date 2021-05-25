package org.pj.module.util.task.wrapper;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import org.pj.module.util.task.TaskTwoArgs;

/**
 * 将Task包装成Runnable
 *
 * @author ZJP
 * @since 2021年05月16日 18:37:20
 **/
final class GameTaskTwoArg<A, B> implements Runnable {

  private static Recycler<GameTaskTwoArg<?, ?>> Recycler = new Recycler<>() {
    @Override
    protected GameTaskTwoArg<?, ?> newObject(Handle<GameTaskTwoArg<?, ?>> handle) {
      return new GameTaskTwoArg<>(handle);
    }
  };

  public static <A,B> Runnable of(TaskTwoArgs<A, B> task, A a, B b) {
    GameTaskTwoArg<A, B> wrapper = (GameTaskTwoArg<A, B>) Recycler.get();
    wrapper.task = task;
    wrapper.a = a;
    wrapper.b = b;
    return wrapper;
  }


  private final Handle<GameTaskTwoArg<?, ?>> handle;
  private A a;
  private B b;
  private TaskTwoArgs<A, B> task;

  public GameTaskTwoArg(Handle<GameTaskTwoArg<?, ?>> handle) {
    this.handle = handle;
  }

  @Override
  public void run() {
    try {
      task.run(a, b);
    } finally {
      recycle();
    }
  }

  public void recycle() {
    a = null;
    b = null;
    task = null;
    handle.recycle(this);
  }
}
