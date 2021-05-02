package org.pj.core.msg.adp;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.pj.core.msg.HandlerInfo;
import org.pj.core.msg.HandlerInfo.ParameterInfo;
import org.pj.core.msg.IAdapter;
import org.pj.core.msg.InvokeContext;

/**
 * @author ZJP
 * @since 2020年06月11日 12:17:43
 **/
public class ContextAdapter implements IAdapter<Object> {

  private static ContextAdapter instance = new ContextAdapter();

  public static ContextAdapter getInstance() {
    return instance;
  }

  private Map<Class<?>, Field> contextFields = Collections.emptyMap();

  private ContextAdapter() {
    init();
  }

  private void init() {
    Field[] fields = InvokeContext.class.getDeclaredFields();
    Map<Class<?>, Field> fieldMap = new HashMap<>();
    for (Field f : fields) {
      if (Modifier.isTransient(f.getModifiers())
          || Modifier.isStatic(f.getModifiers())) {
        continue;
      }

      f.setAccessible(true);
      fieldMap.put(f.getType(), f);
    }
    contextFields = Collections.unmodifiableMap(fieldMap);
  }

  public boolean isContextField(Class<?> clazz) {
    return contextFields.containsKey(clazz);
  }

  @Override
  public Object adapter(InvokeContext context, HandlerInfo info, int idx) throws Exception {
    ParameterInfo parameterInfo = info.getParameterInfos().get(idx);
    Field f = contextFields.get(parameterInfo.getParameter().getType());
    Object result = null;
    if (f != null) {
      result = f.get(context);
    }
    return result;
  }
}
