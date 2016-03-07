
package br.eng.rcc.javamodelo.entidades.embutidos;

import br.eng.rcc.javamodelo.entidades.Usuario;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;

@Embeddable
@Data
public class RegistroUsuario implements Serializable{
  
  @ManyToOne
  protected Usuario usuario;
  
  @Temporal(TemporalType.DATE)
  protected Date atualizacao;
  
  //=======================================================================
  @PrePersist
  public void prePersist(){
    System.out.printf("---  Persist RegistroUsuario \n");
    this.atualizacao = Calendar.getInstance().getTime();
  }
  @PreUpdate
  public void preUpdate(){
    System.out.printf("---  Update RegistroUsuario \n");
    this.atualizacao = Calendar.getInstance().getTime();
  }
  
  
}
