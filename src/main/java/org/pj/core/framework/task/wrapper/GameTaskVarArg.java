package org.pj.core.framework.task.wrapper;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import org.pj.core.framework.task.TaskVarArgs;

/**
 * 将Task包装成Runnable
 *
 * @author ZJP
 * @since 2021年05月16日 18:37:20
 **/
final class GameTaskVarArg implements Runnable {

  private static Recycler<GameTaskVarArg> Recycler = new Recycler<>() {
    @Override
    protected GameTaskVarArg newObject(Handle<GameTaskVarArg> handle) {
      return new GameTaskVarArg(handle);
    }
  };

  public static Runnable of(TaskVarArgs task, Object... args) {
    GameTaskVarArg wrapper = Recycler.get();
    wrapper.task = task;
    wrapper.args = args;
    return wrapper;
  }

  private final Handle<GameTaskVarArg> handle;
  private Object[] args;
  private TaskVarArgs task;

  public GameTaskVarArg(Handle<GameTaskVarArg> handle) {
    this.handle = handle;
  }

  @Override
  public void run() {
    try {
      task.run(args);
    } finally {
      recycle();
    }
  }

  public void recycle() {
    args = null;
    task = null;
    handle.recycle(this);
  }
}
