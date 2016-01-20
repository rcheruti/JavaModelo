
package br.eng.rcc.javamodelo.entidades;

import java.io.Serializable;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
@Cacheable(true)
public class Cor implements Serializable{
  
  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected int id;
  protected String nome;
  protected String hex;
  protected String rgb;
  
  @ManyToOne
  protected Carro carro;
  
}
