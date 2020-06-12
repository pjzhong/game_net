package org.pj.core.event;

import java.lang.reflect.Method;
import java.util.Objects;

public class CallBack {

  private final Object target;
  private final Method method;

  public CallBack(Object target, Method method) {
    this.target = target;
    this.method = method;
  }

  public Object handle(Object... args) throws ReflectiveOperationException {
    return this.method.invoke(this.target, args);
  }

  public Method getMethod() {
    return this.method;
  }

  public Object getTarget() {
    return this.target;
  }

  @Override
  public String toString() {
    return "[wrapper " + this.method + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CallBack callBack = (CallBack) o;
    return target.equals(callBack.target) &&
        method.equals(callBack.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target, method);
  }
}
