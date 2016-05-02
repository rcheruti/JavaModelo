
package br.eng.rcc.framework.utils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class CdiUtils {
  
  public Object getBean(Class<?> klass){
    BeanManager bm = CDI.current().getBeanManager();
    Bean<?> bean = (Bean<?>) bm.getBeans( klass ).iterator().next();
    CreationalContext<?> ctx = bm.createCreationalContext(bean);
    Object objBean = bm.getReference(bean, klass, ctx);
    
    return objBean;
  }
  
}
