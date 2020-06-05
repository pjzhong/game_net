package org.pj.module.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 游戏相关工具类
 *
 * @author xchao
 * @since 2019年4月2日 上午10:52:41
 */
public class GameUtil {

  /**
   * 将配置字符串转化为List<Pair<K, V>>，配置格式 KEY:VALUE;KEY:VALUE
   *
   * @param confString 配置字符串
   * @param keyParser 键解析逻辑
   * @param valParser 值解析逻辑
   */
  public static <K, V> List<Pair<K, V>> toPairList(String confString,
      Function<String, K> keyParser, Function<String, V> valParser) {
    List<Pair<K, V>> pairList = new ArrayList<>();
    if (StringUtils.isBlank(confString)) {
      return pairList;
    }
    String[] pairStrings = confString.split(";");
    for (String pairString : pairStrings) {
      String[] temp = pairString.split(":");
      Pair<K, V> pair = Pair.of(keyParser.apply(temp[0]),
          valParser.apply(temp[1]));
      pairList.add(pair);
    }
    return pairList;
  }

  /**
   * 将配置字符串转化为Pair<K, V>，KEY:VALUE
   *
   * @param s 配置字符串
   * @param keyPar 键解析逻辑
   * @param valPar 值解析逻辑
   */
  public static <K, V> Pair<K, V> toPair(String s, Function<String, K> keyPar,
      Function<String, V> valPar) {
    return toPair(s, ":", keyPar, valPar);
  }

  /**
   * 将配置字符串转化为Pair<K, V>，
   *
   * @param s 配置字符串
   * @param split 分隔符
   * @param keyPar 键解析逻辑
   * @param valPar 值解析逻辑
   */
  public static <K, V> Pair<K, V> toPair(String s, String split,
      Function<String, K> keyPar, Function<String, V> valPar) {
    String[] temp = s.split(split);
    return Pair.of(keyPar.apply(temp[0]), valPar.apply(temp[1]));
  }

  /**
   * 将物品数量字符串转化为List<Pair<String, Integer>> List<<物品Id,数量>>
   */
  public static List<Pair<Integer, Long>> toPairList(String confString) {
    List<Pair<Integer, Long>> pairList = new ArrayList<>();
    if (StringUtils.isBlank(confString)) {
      return pairList;
    }
    String[] pairStrings = confString.split(";");
    for (String pairString : pairStrings) {
      Pair<Integer, Long> pair = toPair(pairString);
      pairList.add(pair);
    }
    return pairList;
  }

  /**
   * 转化为Pair
   *
   * @data 2014年10月16日 下午3:57:26
   */
  public static Pair<Integer, Long> toPair(String confString) {
    if (confString == null) {
      return null;
    }
    String[] pairIds = confString.split(":");
    Pair<Integer, Long> pair = Pair.of(Integer.parseInt(pairIds[0]),
        Long.parseLong(pairIds[1]));
    return pair;
  }
}
