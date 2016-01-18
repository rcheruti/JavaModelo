
package br.eng.rcc.framework.seguranca.entidades;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.interfaces.UsuarioInterface;
import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name="seg_usuario")
@Seguranca(delete = false, select = false, insert = false, update = false)
public class Usuario implements Serializable, UsuarioInterface{ 
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String nome;
    private String email;
    
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "usuario", cascade = CascadeType.PERSIST)
    private Credencial credencial;
    
    
    //=====================================================================
    
    @Override
    public Usuario clone()  {
        Usuario clone = new Usuario();
        clone.id = this.getId(); 
        clone.nome = this.getNome();
        clone.email = this.getEmail();
        
        Credencial cred = this.getCredencial();
        if( cred != null ) clone.credencial = cred.clone();
        
        return null;
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
    
    public String getNome(){ return this.nome; }
    public void setNome(String param){ this.nome = param; }
    
    public String getEmail(){ return this.email; }
    public void setEmail(String param){ this.email = param; }
    
    public Credencial getCredencial(){ return this.credencial; }
    public void setCredencial(Credencial param){ this.credencial = param; }
    
    
}
