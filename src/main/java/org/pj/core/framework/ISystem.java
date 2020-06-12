package org.pj.core.framework;

public interface ISystem {

  void load() throws Exception;

  void reload() throws Exception;

  void init();

  void destroy();
}
