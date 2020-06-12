package org.pj.core.msg.adp;

import com.google.protobuf.Parser;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.pj.core.msg.HandlerInfo;
import org.pj.core.msg.HandlerInfo.ParameterInfo;
import org.pj.core.msg.IAdapter;
import org.pj.core.msg.InvokeContext;

/**
 * @author ZJP
 * @since 2020年06月11日 12:17:43
 **/
public class ProtobufAdapter implements IAdapter<Object> {

  private static ProtobufAdapter instance = new ProtobufAdapter();

  public static ProtobufAdapter getInstance() {
    return instance;
  }

  private Map<Class<?>, Parser<?>> parsers = new ConcurrentHashMap<>();

  private ProtobufAdapter() {
  }

  /**
   * 获取protocol-buffer的解析器
   *
   * @param clazz Protobuf 类
   * @link https://developers.google.cn/protocol-buffers/docs/reference/java-generated?hl=zh-cn#message
   * @since 2020年06月11日 12:17:58
   */
  public Parser<?> extractParser(Class<?> clazz) {
    try {
      return parsers.computeIfAbsent(clazz, this::doParser);
    } catch (Exception ignore) {
    }
    return null;
  }

  private Parser<?> doParser(Class<?> clazz) {
    Parser<?> parser = null;
    try {
      parser = (Parser<?>) clazz
          .getMethod("parser", ArrayUtils.EMPTY_CLASS_ARRAY)
          .invoke(clazz, ArrayUtils.EMPTY_OBJECT_ARRAY);
      return parser;
    } catch (Exception ignore) {
    }
    return null;
  }

  @Override
  public Object adapter(InvokeContext context, HandlerInfo info, int idx) throws Exception {
    ParameterInfo parameterInfo = info.getParameterInfos().get(idx);
    try {

      Parser<?> parser = extractParser(parameterInfo.getParameter().getType());
      Object result = null;
      if (parser != null) {
        result = parser.parseFrom(context.getMessage().getBody());
      }
      return result;
    } catch (Exception e) {
      Parameter p = parameterInfo.getParameter();
      throw new IllegalArgumentException(
          String
              .format("parser protocol parameter %s#%s error", p.getType().getName(), p.getName()),
          e);
    }

  }
}
