
package br.eng.rcc.framework.produtores;

import br.eng.rcc.framework.config.Configuracoes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

@ApplicationScoped
public class EMProducer {
  
  //@PersistenceContext
  //@Produces
  private EntityManager em;
  
  private SessionFactory sf;
  
  
  //@PersistenceUnit
  private EntityManagerFactory emf;
  private boolean myEMF;

  @PostConstruct
  public void postConstruct(){
    System.out.printf("---  Iniciando config de banco \n");
    
    Properties prop = new Properties();
    for(String key : Configuracoes.hibernate.keySet()){
      prop.put(key, Configuracoes.hibernate.get(key));
    }
    
    PersistenceUnitInfoImpl puInfo = 
            new PersistenceUnitInfoImpl("PersistenciaPU", new ArrayList<>(), prop);
    
    EntityManagerFactoryBuilderImpl emFB = 
      new EntityManagerFactoryBuilderImpl(
        new PersistenceUnitInfoDescriptor(puInfo), Collections.emptyMap());
    
    
    emf = emFB.build();
    
    System.out.printf("---  Config de banco pronta! \n");
    
  }
  
  @PreDestroy
  public void preDestroy(){
    if( myEMF && emf.isOpen() ){
      emf.close();
    }
  }

  @Produces
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
