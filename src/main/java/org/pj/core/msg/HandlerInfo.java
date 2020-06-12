package org.pj.core.msg;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class HandlerInfo {

  private final Object handler;
  private final Method method;
  private List<ParameterInfo> parameterInfos;
  private List<IAdapter<?>> adapters;

  public HandlerInfo(Object object, Method method) {
    this.handler = object;
    this.method = method;
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
