
package br.eng.rcc.javamodelo.test.entidades;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@Cacheable(true)
@ToString(exclude = {"carros"})
@EqualsAndHashCode(exclude={"rgb", "carros","hex","nome"})
public class Cor implements Serializable{
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected int id;
  protected String nome;
  protected String hex;
  protected String rgb;
  
  @ManyToMany()
  @JoinTable(name = "ref_carro_cor",           
          joinColumns = @JoinColumn(name="cor_id"),
          inverseJoinColumns = @JoinColumn(name="carro_id")
  )
  protected Set<Carro> carros;
  
}
