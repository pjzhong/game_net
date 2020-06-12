package org.pj.core.thread;

import io.netty.channel.ChannelId;
import io.netty.channel.DefaultChannelId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;

public class GameThreadPoolTest {

  @Test
  public void nettyChannelIdTest() {
    GameThreadPool pool = new GameThreadPool();
    for (int i = 0; i < 100000; i++) {
      ChannelId id = DefaultChannelId.newInstance();
      pool.getPool(id);
    }

    printState(pool);
  }


  @Test
  public void ranDomIdTest() {
    GameThreadPool pool = new GameThreadPool();
    for (int i = 0; i < 100000; i++) {
      pool.getPool(ThreadLocalRandom.current().nextInt(100000));
    }

    printState(pool);
  }

  @Test
  public void sequenceIDTest() {
    GameThreadPool pool = new GameThreadPool();
    for (int i = 0; i < 100000; i++) {
      pool.getPool(i);
    }

    printState(pool);
  }

  private void printState(GameThreadPool pool) {
    Map<Integer, Long> stats = pool.getHashStat();
    List<Long> statList = new ArrayList<>(stats.values());
    System.out.format("min:%s\n", Collections.min(statList));
    System.out.format("max:%s\n", Collections.max(statList));
    System.out.println(stats);
    List<Long> diffs = new ArrayList<>();
    int size = statList.size();
    for (int i = 1; i < size; i++) {
      diffs.add(statList.get(i) - statList.get(i - 1));
    }
    if (1 < size) {
      diffs.add(0, statList.get(size - 1) - statList.get(0));
    }
    System.out.println(diffs);
  }

}
