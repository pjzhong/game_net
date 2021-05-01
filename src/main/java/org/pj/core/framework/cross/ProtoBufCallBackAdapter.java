package org.pj.core.framework.cross;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import java.lang.reflect.Method;
import org.pj.core.msg.MessageProto.Message;
import org.pj.core.msg.adp.ProtobufAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoBufCallBackAdapter implements SocketCallback<Message> {

  private static Logger logger = LoggerFactory.getLogger(SocketCallback.class);

  private final Method method;
  private final SocketCallback<Object> callback;

  public ProtoBufCallBackAdapter(Method method, SocketCallback<?> callback) {
    this.method = method;
    this.callback = (SocketCallback<Object>) callback;
  }

  @Override
  public void accept(Message o) {
    final Class<?> returnType = method.getReturnType();
    Object result = null;
    try {
      if (o.getStat() != 200) {
        logger.error("model {} return state {}", o.getModule(), o.getStat());
      } else {
        if (MessageLite.class.isAssignableFrom(returnType)) {
          ProtobufAdapter adapter = ProtobufAdapter.getInstance();
          Parser<?> parser = adapter.extractParser(returnType);

          result = parser.parseFrom(o.getBody());
        }

        if (result != null || returnType == Void.TYPE) {
          callback.accept(result);
        } else {
          logger.warn("Can't not parse {} of method {}", returnType.getName(), getFullMethodName());
        }
      }
    } catch (Exception e) {
      logger.error("calling {} onSuccess error", getFullMethodName(), e);
    }
  }

  protected String getFullMethodName() {
    return method.getDeclaringClass().getName() + "." + method.getName();
  }
}
