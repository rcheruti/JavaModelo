
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
    
    if( em.isOpen() ){
      if( !em.isJoinedToTransaction() ){
        try{
          em.joinTransaction();
        }catch(TransactionRequiredException ex){
          em.getTransaction().begin();
        }
      }
      return ctx.proceed();
    }else{
      throw new RuntimeException("EM não está aberto!");
    }
  }
  
}
