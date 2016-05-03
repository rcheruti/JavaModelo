
package br.eng.rcc.framework.seguranca.entidades;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@Entity
@Table(name="seg_grupo")
@Seguranca(delete = false, select = false, insert = false, update = false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Grupo implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    
    protected String nome;
    protected String chave;
    
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "grupos")
    protected Set<Credencial> credenciais;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "seg_fk_grupo_permissao")
    protected Set<Permissao> permissoes;
    
    
    //========================================================================
    
    public Grupo clone(){
        Grupo copia = new Grupo();
        
        copia.id = this.getId(  );
        copia.nome = this.getNome(  );
        copia.chave = this.getChave(  );
        copia.credenciais = null;
        
        Set<Permissao> permissoesC = this.getPermissoes();
        if( permissoesC != null ){
            Set<Permissao> perms = new HashSet<>( permissoesC.size() );
            for( Permissao p : permissoesC ){
                perms.add( p.clone() );
            }
            copia.permissoes = perms;
        }
        
        return copia;
    }
    
    
    @Override
    public int hashCode(){
        return id;
    }
    @Override
    public boolean equals(Object objT){
        if( this == objT )return true;
        if( !( objT instanceof Grupo ) ) return false;
        Grupo obj = (Grupo) objT;
        if( this.getId() == obj.getId()
                ) return true;
        return false;
    }
    
    
    
    public boolean hasPermissao(String perm){
        if( perm == null ) return false;
        if( getPermissoes() != null ){
            for( Permissao p : getPermissoes() ){
                if( perm.equals( p.getNome() ) ) return true;
            }
        }
        return false;
    }
    
    //========================================================================
    
    public int getId(){ return this.id; }
    public void setId(int param){ this.id = param; }
    
    public String getNome(){ return this.nome; }
    public void setNome(String param){ this.nome = param; }
    
    public String getChave(){ return this.chave; }
    public void setChave(String param){ this.chave = param; }
    
    public Set<Credencial> getCredenciais(){ return this.credenciais; }
    public void setCredenciais(Set<Credencial> param){ this.credenciais = param; }
    
    public Set<Permissao> getPermissoes(){ return this.permissoes; }
    public void setPermissoes(Set<Permissao> param){ this.permissoes = param; }
    
}
