
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
import javax.persistence.PersistenceUnit;

@ApplicationScoped
public class EMProducer {
    
  @PersistenceUnit
  private EntityManagerFactory emf;
  private boolean myEMF;

  @PostConstruct
  public void postConstruct(){
    if( Configuracoes.persistenceUnit != null && !Configuracoes.persistenceUnit.isEmpty() ){
      myEMF = true;
      emf = Persistence.createEntityManagerFactory(Configuracoes.persistenceUnit);
    }
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

  public void disposeEM(@Disposes EntityManager em){
    if( em.isOpen() ){
      em.flush();
      em.close();
    }
  }
    
}
