
package br.eng.rcc.javamodelo.entidades;

import java.io.Serializable;
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
@ToString(exclude = {"porta"}) 
@EqualsAndHashCode(exclude={"porta"})
public class Janela implements Serializable{
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected int id;
  
  protected boolean eletrica;
  
  @ManyToOne
  protected Porta porta;
  
}
