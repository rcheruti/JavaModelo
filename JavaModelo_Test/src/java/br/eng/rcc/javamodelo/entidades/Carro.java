
package br.eng.rcc.javamodelo.entidades;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = {"cores"})
public class Carro implements Serializable{
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected int id;
  
  protected String nome;
  
  @OneToMany(mappedBy = "carro")
  protected List<Cor> cores;
  
  
}
