
package br.eng.rcc.javamodelo.entidades;

import javax.persistence.Id;
import lombok.Data;

@Data
public class Carro {
  
  @Id
  private int id;
  private String cor;
  
}
