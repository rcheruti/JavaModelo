
package br.eng.rcc.framework.seguranca.entidades;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 
 * @author Rafael
 */
@Entity
@Table(name="seg_chave_acesso")
@Seguranca(delete = false, select = false, insert = false, update = false)
public class ChaveAcesso implements Serializable{
    
    @Id
    private String chave;
    
    
    @ManyToOne
    private Usuario usuario;
    
    
    
    //==========  Getters e Setters  ==========
    public String getChave(){ return this.chave; }
    public void setChave(String x){ this.chave = x; }
    
    public Usuario getUsuario(){ return this.usuario; }
    public void setUsuario(Usuario x){ this.usuario = x; }
    
}
