
package br.eng.rcc.javamodelo.entidades;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = {"cores"})  
@EqualsAndHashCode(exclude={"cores", "portas","valor"})
public class Carro implements Serializable{
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected int id;
  
  protected String nome;
  
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "carro" )
  protected Valor valor;
  
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "carro")
  protected Set<Porta> portas;
    public void setPortas(Set<Porta> xs){
      portas = xs;
      if(xs != null) for(Porta x : xs) x.setCarro(this);
    }
  
  
  @ManyToMany() // cascade = CascadeType.ALL
  @JoinTable(name = "ref_carro_cor",
          joinColumns = @JoinColumn(name="carro_id"),
          inverseJoinColumns = @JoinColumn(name="cor_id")
  )
  protected Set<Cor> cores;
  
  
  
}
