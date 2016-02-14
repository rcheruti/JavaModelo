
package br.eng.rcc.framework.seguranca.predicados;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class NullSupplier implements Supplier<Predicate>{
  
  @Override
  public Predicate get() {
    return null;
  }
  
}
