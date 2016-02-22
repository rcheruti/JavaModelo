
package br.eng.rcc.framework.seguranca.filtros;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UrlSeguranca implements AnnotatedElement{
  
  protected List<Annotation> annotations = new ArrayList<>(5);
  
  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    for( Annotation ann : annotations ){
      if( annotationClass != null && annotationClass.isAssignableFrom(ann.getClass()) ) return (T) ann;
    }
    return null;
  }

  @Override
  public Annotation[] getAnnotations() {
    return Arrays.copyOf(annotations.toArray(new Annotation[0]), annotations.size() );
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }
  
  public List<Annotation> getList(){ return this.annotations; }
  
}
