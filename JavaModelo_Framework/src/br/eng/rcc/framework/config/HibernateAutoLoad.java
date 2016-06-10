
package br.eng.rcc.framework.config;

import java.util.Collection;
import java.util.Set;
import javax.persistence.Entity;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@HandlesTypes({Entity.class})
public class HibernateAutoLoad implements ServletContainerInitializer{
  
  private static Logger log = LogManager.getLogger();
  
  
  @Override
  public void onStartup(Set<Class<?>> set, ServletContext sc) throws ServletException {
    Configuracoes.carregar();
    
    if( !Configuracoes.getInstance().hibernateAutoLoad() ) return;
    
    if( set != null && !set.isEmpty() ){
      log.debug("HibernateAutoLoad: carregando classes:");
      System.out.printf("---  HibernateAutoLoad: carregando classes: \n");
      Collection<String> classes = Configuracoes.getInstance().entidadesClasses();
      for(Class<?> k : set){
        log.debug("---  Classe: {}", k.getCanonicalName());
        classes.add( k.getCanonicalName() );
      }
      Configuracoes.salvar();
    }
  }
  
  
  
}
