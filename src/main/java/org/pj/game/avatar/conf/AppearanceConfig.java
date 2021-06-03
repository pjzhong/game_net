package org.pj.game.avatar.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.pj.game.conf.Config;
import org.pj.game.item.config.AbsTemplate;
import org.pj.game.util.GameUtil;

/**
 * 外观共同配置
 *
 * @author ZJP
 * @since 2020年06月12日 14:47:16
 **/
public class AppearanceConfig extends AbsTemplate {

  /** 模型资源 */
  @Config
  private int modelRes;
  /** 属性 */
  @Config(alias = "additionalAttributes", parser = "parseAttr")
  private Map<Short, Double> attrs;
  /** 激活条件 */
  @Config(parser = "toPair", parserClass = GameUtil.class)
  private Pair<Integer, Long> open;
  /** 0不过期 */
  @Config(alias = "MaxTime", allowNull = true)
  private long expire;

  public int getModelRes() {
    return modelRes;
  }

  public Map<Short, Double> getAttrs() {
    return attrs;
  }

  public Pair<Integer, Long> getOpen() {
    return open;
  }

  public long getExpire() {
    return expire;
  }

  private Map<Short, Double> parseAttr(String s) {
    Map<Short, Double> res = new HashMap<>();
    String[] blocks = s.split(";");
    for (String b : blocks) {
      String[] temp = b.split(":");
      res.put(Short.parseShort(temp[0]), Double.parseDouble(temp[1]));
    }
    return Collections.unmodifiableMap(res);
  }
}
