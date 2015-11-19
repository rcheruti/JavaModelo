
package br.eng.rcc.seguranca.entidades;

import br.eng.rcc.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name="seg_usuario")
//@Seguranca(delete = false, select = false, insert = false, update = false)
public class Usuario implements Serializable{
    
    @Id
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
