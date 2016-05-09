
package br.eng.rcc.framework.produtores;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;

@Interceptor
@Transactional
public class Transacional {
  
  @Inject
  private EntityManager em;
  
  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Exception{
    open(em);
    Object obj = ctx.proceed();
    
    return obj;
  }
  
  public static void open(EntityManager em){
    System.out.printf("---  Interceptando Transactional (open) \n");
    
    if( em != null && em.isOpen() ){
      if( !em.isJoinedToTransaction() ){
        try{
          EntityTransaction eT = em.getTransaction();
          eT.begin();
        }catch(IllegalStateException ex){
          em.joinTransaction();
        }
      }
    }else{
      throw new RuntimeException("EM não está aberto!");
    }
  }
  public static void close(EntityManager em){
    System.out.printf("---  Interceptando Transactional (close) \n");
    
    if( em != null && em.isOpen() ){
      try{
        EntityTransaction eT = em.getTransaction();
        if( eT.isActive() ){
          eT.commit();
        }
      }catch(IllegalStateException ex){
        em.flush();
      }
    }
  }
  
}
