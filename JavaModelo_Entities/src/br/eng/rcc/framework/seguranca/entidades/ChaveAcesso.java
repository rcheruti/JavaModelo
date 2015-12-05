
package br.eng.rcc.framework.seguranca.entidades;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * 
 * @author Rafael
 */
@Entity
public class ChaveAcesso implements Serializable{
    
    @Id
    private String chave;
    
    @OneToOne
    private Usuario usuario;
    
    
    
    //==========  Getters e Setters  ==========
    public String getChave(){ return this.chave; }
    public void setChave(String x){ this.chave = x; }
    
    public Usuario getUsuario(){ return this.usuario; }
    public void setUsuario(Usuario x){ this.usuario = x; }
    
}
