package org.pj.core.framework.disruptor;

import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * 任务转换器
 *
 * @author ZJP
 * @since  2021年02月19日 16:53:59
 **/
public class RunnableTranslator implements EventTranslatorOneArg<GameRunnable, Runnable> {

  public static final RunnableTranslator INSTANCE = new RunnableTranslator();

  private RunnableTranslator() {
  }

  @Override
  public void translateTo(GameRunnable event, long sequence, Runnable run) {
    event.setName("起个名字?");
    event.setRunnable(run);
  }
}
