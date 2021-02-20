package org.pj.core.framework.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * 游戏任务执行器(尽量保持无状态)
 *
 * @author ZJP
 * @since 2021年02月19日 18:15:17
 **/
public class GameRunnableHandler implements EventHandler<GameRunnable> {

  @Override
  public void onEvent(GameRunnable event, long sequence, boolean endOfBatch) throws Exception {
    try {
      event.run();
    } finally {
      event.clear();
    }
  }
}
