
package br.eng.rcc.framework.seguranca.entidades;

import java.io.Serializable;
import javax.persistence.*;
import br.eng.rcc.framework.interfaces.IUsuario;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;

@Entity
@Table(name="seg_usuario")
@Seguranca(delete = false, select = false, insert = false, update = false)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue(value="0")
public abstract class SegUsuario implements Serializable, IUsuario{ 
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "usuario", cascade = CascadeType.PERSIST)
    protected Credencial credencial;
    
    @Column(updatable = false, insertable = false)
    protected int DTYPE = 0;
    
    //=====================================================================
    
    @Override
    public SegUsuario clone(){
        try{
          SegUsuario clone = this.getClass().newInstance();

          Credencial cred = this.getCredencial();
          if( cred != null ) clone.credencial = cred.clone();

          return clone;
        }catch(InstantiationException | IllegalAccessException ex){
          return null;
        }
    }
    
    @Override
    public boolean hasPermissao(String chave){
        return this.getCredencial().hasPermissao(chave);
    }
    @Override
    public boolean hasGrupo(String chave){
        return this.getCredencial().hasGrupo(chave);
    }
    
    //=====================================================================
    
    public int getId(){ return this.id; }
    public void setId(int param){ this.id = param; }
    
    public Credencial getCredencial(){ return this.credencial; }
    public void setCredencial(Credencial param){ this.credencial = param; }
    
    
    
}
