
package br.eng.rcc.framework.seguranca.entidades;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="seg_chave_acesso")
@Seguranca(delete = false, select = false, insert = false, update = false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class ChaveAcesso implements Serializable {
    
    @Id @Column(length = 255, unique = true)
    protected String chave;
    
    @Temporal(TemporalType.DATE)
    protected Date criacao = Calendar.getInstance().getTime();
    
    @ManyToOne
    protected Credencial credencial;
    
    
    @Override
    public ChaveAcesso clone(){
      ChaveAcesso clone = new ChaveAcesso();
      clone.credencial = null;
      clone.chave = null;
      clone.criacao = this.getCriacao();
      
      return clone;
    }
    
    
    //==========  Getters e Setters  ==========
    public String getChave(){ return this.chave; }
    public void setChave(String x){ this.chave = x; }
    
    public Credencial getCredencial(){ return this.credencial; }
    public void setCredencial(Credencial x){ this.credencial = x; }
    
    public Date getCriacao(){ return this.criacao; }
    public void setCriacao(Date x){ this.criacao = x; }
    
}
