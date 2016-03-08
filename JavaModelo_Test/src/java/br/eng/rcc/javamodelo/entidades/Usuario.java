package br.eng.rcc.javamodelo.entidades;

import br.eng.rcc.framework.seguranca.entidades.Credencial;
import br.eng.rcc.framework.seguranca.entidades.SegUsuario;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@DiscriminatorValue(value = "1")
@ToString(exclude = {"DTYPE"}) // ,"credencial"
public class Usuario extends SegUsuario {

  protected String nome;
  protected String email;

  @Column(updatable = false, insertable = false)
  protected int DTYPE = 1;

  
  @Override
  public Usuario clone() {
    Usuario clone = new Usuario();
    
    clone.id = this.id;
    clone.nome = this.nome;
    clone.email = this.email;
    
    Credencial cred = this.getCredencial();
    if (cred != null) {
      clone.credencial = cred.clone();
    }

    return clone;
  }

}
