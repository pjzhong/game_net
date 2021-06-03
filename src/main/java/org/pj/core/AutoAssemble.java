package org.pj.core;

import org.pj.core.anno.Facade;
import org.pj.core.framework.ISystem;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AutoAssemble {


  public static void checkIocBean(AnnotationConfigApplicationContext context) {
    context.getBeansOfType(ISystem.class);
    context.getBeansWithAnnotation(Facade.class);

  }
}
