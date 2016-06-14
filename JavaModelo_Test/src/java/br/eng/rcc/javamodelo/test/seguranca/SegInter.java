
package br.eng.rcc.javamodelo.test.seguranca;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.persistencia.MsgException;
import br.eng.rcc.framework.utils.Busca;
import br.eng.rcc.javamodelo.test.entidades.Carro;

public class SegInter implements SegurancaPersistenciaInterceptador<Carro>{
  
  @Override
  public void filter(Busca busca){
    System.out.printf("---  Checando segurança de Buscar no interceptador \n\n");
    //throw new MsgException("Não vou deixar você ver isso!");
  }
  
}
