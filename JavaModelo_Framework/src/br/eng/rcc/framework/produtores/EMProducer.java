
package br.eng.rcc.framework.produtores;

import br.eng.rcc.framework.config.Configuracoes;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;

@ApplicationScoped
public class EMProducer {
  
  private static Logger log = LogManager.getLogger();
  
  private EntityManagerFactory emf;
  private boolean myEMF;

  @PostConstruct
  public void postConstruct(){
    log.info("Iniciando config de banco");
    
    Properties prop = new Properties();
    prop.putAll(Configuracoes.getInstance().hibernate());
    
    List<URL> jarUrls = new ArrayList<>();
    //jarUrls.add( this.getClass().getClassLoader().getResource("../lib/JavaModelo_Entities.jar") );
    
    List<String> entidadesClasses = new ArrayList<>(40);
    entidadesClasses.addAll(Configuracoes.getInstance().entidadesClasses());
    
    PersistenceUnitInfoImpl puInfo = 
            new PersistenceUnitInfoImpl("PersistenciaPU", 
                    entidadesClasses
                    , prop, jarUrls);
    
    EntityManagerFactoryBuilder emFB = 
      Bootstrap.getEntityManagerFactoryBuilder(
        new PersistenceUnitInfoDescriptor(puInfo), Collections.emptyMap(), 
              this.getClass().getClassLoader());
    
    URL url = this.getClass().getClassLoader().getResource("../lib/JavaModelo_Entities.jar");
    log.debug("Resource URL: {}", url);
    
    emf = emFB.build();
    
    log.info("Config de banco pronta!");
  }
  
  @PreDestroy
  public void preDestroy(){
    if( myEMF && emf.isOpen() ){
      emf.close();
    }
  }

  @Produces
  @RequestScoped
  public EntityManager produceEM(){
    return emf.createEntityManager();
  }
  
            // @Disposes
  public void disposeEM( EntityManager em){
    if( em.isOpen() ){
      em.flush();
      try{
        if( em.getTransaction().isActive() ){
          em.getTransaction().commit();
        }
      }catch(IllegalStateException ex){
        
      }
      em.close();
    }
  }
    
}
