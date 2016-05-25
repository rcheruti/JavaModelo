
package br.eng.rcc.framework.config;

import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

@HandlesTypes({Entity.class})
public class HibernateAutoLoad implements ServletContainerInitializer{

  @Override
  public void onStartup(Set<Class<?>> set, ServletContext sc) throws ServletException {
    if( set != null && !set.isEmpty() ){
      System.out.printf("---  HibernateAutoLoad: carregando classes: \n");
      List<String> classes = Configuracoes.getInstance().entidadesClasses();
      for(Class<?> k : set){
        System.out.printf("---  Classe: %s \n", k.getCanonicalName() );
        classes.add( k.getCanonicalName() );
      }
    }
  }
  
  
  
}
