
package br.eng.rcc.framework.testes.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ApplicationScoped
public class EMProducer {
  
  private EntityManagerFactory emf;
  
  @Produces
  @ApplicationScoped
  public EntityManagerFactory criarEMF(){
    if( emf == null ){
      emf = Persistence.createEntityManagerFactory("H2_TestePU");
    }
    return emf;
  }
  public void destruirEMF(@Disposes EntityManagerFactory emf){
    System.out.printf("Fechando EMF \n");
    emf.close();
  }
  
  
  @Produces
  @RequestScoped
  public EntityManager criarEM(){
    if( emf == null ) emf = criarEMF();
    return emf.createEntityManager();
  }
  public void destruirEM(@Disposes EntityManager em){
    System.out.printf("Fechando EM, trans: %s \n", em.getTransaction().isActive() );
    if(em.getTransaction().isActive()){
      em.getTransaction().rollback();
    }
    em.close();
  }
  
  
}
