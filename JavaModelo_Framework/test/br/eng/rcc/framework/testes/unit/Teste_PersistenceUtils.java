
package br.eng.rcc.framework.testes.unit;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Test;

@Dependent
public class Teste_PersistenceUtils {
  
  @Inject
  private EntityManager em;
  
  @Test
  public void testarNumero(){
    Assert.assertTrue( 5 == 3+2 );
    Assert.assertTrue( em != null );
  }
  @Test
  public void testarString(){
    Assert.assertEquals( "Coisa", "Coisa" );
  }
    
}
