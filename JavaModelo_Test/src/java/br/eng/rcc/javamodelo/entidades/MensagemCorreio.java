
package br.eng.rcc.javamodelo.entidades;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.javamodelo.entidades.embutidos.RegistroUsuario;
import br.eng.rcc.javamodelo.seguranca.SerUsuarioLogadoFilter;
import java.io.Serializable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
@Seguranca(filters={ SerUsuarioLogadoFilter.class })
public class MensagemCorreio implements Serializable{
  
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  private String mensagem;
  
  @Seguranca("registro_usuario")
  @Embedded
  private RegistroUsuario registroUsuario;
  
}
