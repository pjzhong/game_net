package org.pj.core.msg;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

public class HandlerInfo {

  private final Object handler;
  private final Method method;
  private List<ParameterInfo> parameterInfos;
  private List<IAdapter<?>> adapters;
  private ThreadLocal<Object[]> paramsArray;

  public HandlerInfo(Object object, Method method) {
    this.handler = object;
    this.method = method;
    this.paramsArray = new ThreadLocal<>();
  }

  public void setParameterInfos(List<ParameterInfo> parameterInfos) {
    this.parameterInfos = parameterInfos;
  }

  public void setAdapters(List<IAdapter<?>> adapters) {
    this.adapters = adapters;
  }

  public Object getHandler() {
    return handler;
  }

  public Method getMethod() {
    return method;
  }

  public List<ParameterInfo> getParameterInfos() {
    return parameterInfos;
  }

  public List<IAdapter<?>> getAdapters() {
    return adapters;
  }

  public Object[] paramArray() {
    Object[] result = paramsArray.get();
    if(result == null) {
      paramsArray.set(ObjectUtils.isEmpty(parameterInfos) ? ArrayUtils.EMPTY_OBJECT_ARRAY : new Object[parameterInfos.size()]);
      result = paramsArray.get();
    }

    return result;
  }

  public static class ParameterInfo {

    private final Parameter parameter;
    private boolean isRequired;

    public ParameterInfo(Parameter parameter) {
      this.parameter = parameter;
    }

    public Parameter getParameter() {
      return parameter;
    }

    public boolean isRequired() {
      return isRequired;
    }

    public void setRequired(boolean required) {
      isRequired = required;
    }
  }

}
