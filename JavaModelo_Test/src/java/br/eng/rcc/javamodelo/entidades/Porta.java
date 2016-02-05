
package br.eng.rcc.javamodelo.entidades;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = {"carro"}) 
@EqualsAndHashCode(exclude={"cor", "carro"})
public class Porta implements Serializable{
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected int id;
  
  @ManyToOne()
  protected Carro carro;
  
  protected String cor;
  
}
