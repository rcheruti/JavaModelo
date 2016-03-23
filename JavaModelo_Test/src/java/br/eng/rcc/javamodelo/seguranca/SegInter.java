
package br.eng.rcc.javamodelo.seguranca;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.javamodelo.entidades.Carro;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class SegInter implements SegurancaPersistenciaInterceptador<Carro>{
  
  public void check(CriteriaBuilder cb, CriteriaQuery query){
    System.out.printf("---  Checando segunrança de Buscar no interceptador \n\n");
    //throw new MsgException("Não vou deixar você ver isso!");
  }
  
}
