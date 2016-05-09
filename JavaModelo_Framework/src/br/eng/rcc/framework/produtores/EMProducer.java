
package br.eng.rcc.framework.produtores;

import br.eng.rcc.framework.config.Configuracoes;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
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
  
  
  
  @Produces
  @RequestScoped
  public EntityManager getEntityManager(){
    return emf.createEntityManager();
  }
  @Produces
  @DependentEM
  @Dependent
  public EntityManager getEntityManager_Dependent(){
    return emf.createEntityManager();
  }
  
  public void closeEntityManager( @Disposes @Any EntityManager em ){
    System.out.printf("---  @Disposes EntityManager \n");
    try{
      if( em.isOpen() ){
        em.flush();
        if( em.getTransaction().isActive() ){
          em.getTransaction().commit();
        }
        em.clear();
        em.close();
      }
    }catch(IllegalStateException ex){
      System.out.printf("--- EX.: %s \n", ex.getMessage() );
    }
  }
  
  
  @PostConstruct
  public void postContruct(){
    System.out.printf("---  EMProducer: postContruct. \n");
    
    if( Configuracoes.persistenceUnit != null && !Configuracoes.persistenceUnit.isEmpty() ){
      System.out.printf("---  Criando novo EMF com a PU: %s \n", Configuracoes.persistenceUnit );
      myEMF = true;
      emf = Persistence.createEntityManagerFactory(Configuracoes.persistenceUnit);
      //
    }
    if( emf == null ){
      throw new RuntimeException("Não encontramos uma PU! Configure o nome da PU em 'web.xml', no parametro 'Configuracoes.persistenceUnit'.");
    }
    
  }
  @PreDestroy
  public void preDestroy(){
    System.out.printf("---  EMProducer: preDestroy. \n");
    emf.getCache().evictAll();
    if( myEMF && emf.isOpen() ){
      System.out.printf("---  Fechando EMF. \n");
      emf.close();
    }
  }
  
  
  
}
