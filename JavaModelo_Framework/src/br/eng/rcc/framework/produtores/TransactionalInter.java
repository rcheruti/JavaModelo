
package br.eng.rcc.framework.produtores;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.transaction.Transactional;

@Interceptor
@Transactional
public class TransactionalInter {
  
  @Inject
  private EntityManager em;
  
  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Exception{
    System.out.printf("--- Interceptando Transactional \n");
    
    open(em);
    return ctx.proceed();
  }
  
  public static void open(EntityManager em){
    if( em.isOpen() ){
      if( !em.isJoinedToTransaction() ){
        try{
          em.joinTransaction();
          if( !em.isJoinedToTransaction() ) em.getTransaction().begin();
        }catch(TransactionRequiredException ex){
          em.getTransaction().begin();
        }
      }
    }else{
      throw new RuntimeException("EM não está aberto!");
    }
  }
  public static void commit(EntityManager em){
    if( em.isOpen() ){
      if( em.isJoinedToTransaction() ){
        
      }else if( em.getTransaction().isActive() ){
        em.getTransaction().commit();
      }
    }
  }
  public static void rollback(EntityManager em){
    if( em.isOpen() ){
      if( em.isJoinedToTransaction() ){
        
      }else if( em.getTransaction().isActive() ){
        em.getTransaction().setRollbackOnly();
        em.getTransaction().rollback();
      }
    }
  }
}
