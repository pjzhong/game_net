package org.pj.game.util.task.wrapper;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import org.pj.game.util.task.TaskThreeArgs;

/**
 * 将Task包装成Runnable
 *
 * @author ZJP
 * @since 2021年05月16日 18:37:20
 **/
final class GameTaskThreeArg<A, B, C> implements Runnable {

  private static Recycler<GameTaskThreeArg<?, ?, ?>> Recycler = new Recycler<>() {
    @Override
    protected GameTaskThreeArg<?, ?, ?> newObject(Handle<GameTaskThreeArg<?, ?, ?>> handle) {
      return new GameTaskThreeArg<>(handle);
    }
  };


  public static <A,B,C> Runnable of(TaskThreeArgs<A, B, C> task, A a, B b, C c) {
    GameTaskThreeArg<A, B, C> wrapper = (GameTaskThreeArg<A, B, C>) Recycler.get();
    wrapper.task = task;
    wrapper.a = a;
    wrapper.b = b;
    wrapper.c = c;
    return wrapper;
  }

  private final Handle<GameTaskThreeArg<?, ?, ?>> handle;
  private A a;
  private B b;
  private C c;
  private TaskThreeArgs<A, B, C> task;

  public GameTaskThreeArg(Handle<GameTaskThreeArg<?, ?, ?>> handle) {
    this.handle = handle;
  }

  @Override
  public void run() {
    try {
      task.run(a, b, c);
    } finally {
      recycle();
    }
  }

  public void recycle() {
    a = null;
    b = null;
    c = null;
    task = null;
    handle.recycle(this);
  }
}
