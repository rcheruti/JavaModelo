
package br.eng.rcc.framework.utils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class CdiUtils {
  
  public static <T> T getBean(Class<T> klass){
    BeanManager bm = CDI.current().getBeanManager();
    Bean<T> bean = (Bean<T>) bm.getBeans( klass ).iterator().next();
    CreationalContext<T> ctx = bm.createCreationalContext(bean);
    Object objBean = bm.getReference(bean, klass, ctx);
    
    return (T) objBean;
  }
  
}
