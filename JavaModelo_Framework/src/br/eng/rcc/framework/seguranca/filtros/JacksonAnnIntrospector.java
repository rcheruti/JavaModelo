
package br.eng.rcc.framework.seguranca.filtros;

import br.eng.rcc.framework.utils.ClassCache;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class JacksonAnnIntrospector extends JacksonAnnotationIntrospector{
  
  private ClassCache cache;
  
  public JacksonAnnIntrospector(ClassCache cache){
    this.cache = cache;
  }
  
  @Override
  public Object findFilterId( Annotated ann ){
    Object id = super.findFilterId(ann);
    if( id == null ){
      Class<?> klass = ann.getRawType();
      klass = cache.get( klass.getSimpleName(), null, true );
      if( klass != null ) id = "seguranca";
    }
    return id;
  }
  
}
