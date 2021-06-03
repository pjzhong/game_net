package org.pj.game.util.task.wrapper;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import org.pj.game.util.task.TaskOneArg;

/**
 * 将Task包装成Runnable
 *
 * @author ZJP
 * @since 2021年05月16日 18:37:20
 **/
final class GameTaskOneArg<A> implements Runnable {

  private static Recycler<GameTaskOneArg<?>> Recycler = new Recycler<>() {
    @Override
    protected GameTaskOneArg<?> newObject(Handle<GameTaskOneArg<?>> handle) {
      return new GameTaskOneArg(handle);
    }
  };

  public static <A> Runnable of(TaskOneArg<A> task, A a) {
    GameTaskOneArg<A> wrapper = (GameTaskOneArg<A>) Recycler.get();
    wrapper.task = task;
    wrapper.a = a;
    return wrapper;
  }

  private final Handle<GameTaskOneArg<A>> handle;
  private A a;
  private TaskOneArg<A> task;

  public GameTaskOneArg(Handle<GameTaskOneArg<A>> handle) {
    this.handle = handle;
  }

  @Override
  public void run() {
    try {
      task.run(a);
    } finally {
      recycle();
    }
  }

  public void recycle() {
    a = null;
    task = null;
    handle.recycle(this);
  }
}
