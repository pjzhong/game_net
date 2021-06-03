package org.pj.game.util.task.wrapper;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import org.pj.game.util.task.TaskFourArgs;

/**
 * 将Task包装成Runnable
 *
 * @author ZJP
 * @since 2021年05月16日 18:37:20
 **/
final class GameTaskFourArg<A, B, C, D> implements Runnable {

  private static Recycler<GameTaskFourArg<?, ?, ?, ?>> Recycler = new Recycler<>() {
    @Override
    protected GameTaskFourArg<?, ?, ?, ?> newObject(Handle<GameTaskFourArg<?, ?, ?, ?>> handle) {
      return new GameTaskFourArg<>(handle);
    }
  };


  public static <A, B, C, D> Runnable of(TaskFourArgs<A, B, C, D> task, A a, B b, C c, D d) {
    GameTaskFourArg<A, B, C, D> wrapper = (GameTaskFourArg<A, B, C, D>) Recycler.get();
    wrapper.task = task;
    wrapper.a = a;
    wrapper.b = b;
    wrapper.c = c;
    wrapper.d = d;
    return wrapper;
  }

  private final Handle<GameTaskFourArg<?, ?, ?, ?>> handle;
  private A a;
  private B b;
  private C c;
  private D d;
  private TaskFourArgs<A, B, C, D> task;

  public GameTaskFourArg(Handle<GameTaskFourArg<?, ?, ?, ?>> handle) {
    this.handle = handle;
  }

  @Override
  public void run() {
    try {
      task.run(a, b, c, d);
    } finally {
      recycle();
    }
  }

  public void recycle() {
    a = null;
    b = null;
    c = null;
    d = null;
    task = null;
    handle.recycle(this);
  }
}
