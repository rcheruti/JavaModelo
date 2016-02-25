
package br.eng.rcc.framework.testes.config;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class WeldJUnit4Runner extends BlockJUnit4ClassRunner{
  
  public WeldJUnit4Runner(Class x) throws InitializationError{
    super(x);
  }
  
  @Override
  protected Object createTest() {
    final Class<?> test = getTestClass().getJavaClass();
    System.out.printf("Criando pelo WELD: %s \n", test);
    Object x = WeldContext.INSTANCE.getBean(test);
    System.out.printf("Criando pelo WELD: %s \n", x.getClass());
    return x;
  }
  
}
