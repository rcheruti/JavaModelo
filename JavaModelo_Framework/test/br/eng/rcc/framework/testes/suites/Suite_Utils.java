
package br.eng.rcc.framework.testes.suites;

import br.eng.rcc.framework.testes.config.WeldJUnit4Suite;
import br.eng.rcc.framework.testes.config.Config;
import br.eng.rcc.framework.testes.unit.Teste_PersistenceUtils;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(WeldJUnit4Suite.class)
@Suite.SuiteClasses({
  Teste_PersistenceUtils.class
})
public class Suite_Utils {
  
  @BeforeClass
  public static void setup(){
    System.out.printf("Iniciando Suite_Utils: \n");
    //Config.initConfig();
  }
  
}
