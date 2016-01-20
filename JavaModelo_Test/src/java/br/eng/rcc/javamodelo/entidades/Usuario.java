
package br.eng.rcc.javamodelo.entidades;

import br.eng.rcc.framework.seguranca.entidades.SegUsuario;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@DiscriminatorValue(value="1")
@ToString(exclude = {"DTYPE","credencial"})
public class Usuario extends SegUsuario{ 
  
  
  private String nome;
  private String email;
  
  @Column(updatable = false, insertable = false)
  protected int DTYPE = 1;
    
}
