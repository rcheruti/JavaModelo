
package br.eng.rcc.framework.produtores;

import br.eng.rcc.framework.config.Configuracoes;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import org.hibernate.cfg.Configuration;

@ApplicationScoped
public class EMProducer {
  
  //@PersistenceContext
  //@Produces
  private EntityManager em;
  
  
  //@PersistenceUnit
  private EntityManagerFactory emf;
  private boolean myEMF;

  @PostConstruct
  public void postConstruct(){
    
    Configuration cfg = new Configuration()
        .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect")
        .setProperty("hibernate.connection.datasource", "java:comp/env/JavaModelo_Test")
        ;
    
    cfg.buildSessionFactory();
    
  }
  
  @PreDestroy
  public void preDestroy(){
    if( myEMF && emf.isOpen() ){
      emf.close();
    }
  }

  //@Produces
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
