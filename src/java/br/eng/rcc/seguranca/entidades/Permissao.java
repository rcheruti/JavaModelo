
package br.eng.rcc.seguranca.entidades;

import br.eng.rcc.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@Entity
@Table(name="seg_permissao")
@Seguranca(delete = false, select = false, insert = false, update = false)
public class Permissao implements Serializable{
    
    @Id 
    private int id;
    
    private String nome;
    
    
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissoes")
    private Set<Grupo> grupos;
    
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissoes")
    private Set<Credencial> credenciais;
    
    
    
    //==================================================================
    
    public Permissao clone(){
        Permissao copia = new Permissao();
        copia.id = this.id;
        copia.nome = this.nome;
        
        return copia;
    }
    
    @Override
    public int hashCode(){
        return id;
    }
    @Override
    public boolean equals(Object objT){
        if( this == objT )return true;
        if( !( objT instanceof Permissao ) ) return false;
        Permissao obj = (Permissao) objT;
        if( this.getId() == obj.getId()
                ) return true;
        return false;
    }
    
    
    
    //==================================================================
    
    public int getId(){ return this.id; }
    public void setId(int param){ this.id = param; }
    
    public String getNome(){ return this.nome; }
    public void setId(String param){ this.nome = param; }
    
    public Set<Grupo> getGrupos(){ return this.grupos; }
    public void setGrupos(Set<Grupo> param){ this.grupos = param; }
    
    public Set<Credencial> getCredenciais(){ return this.credenciais; }
    public void setCredenciais(Set<Credencial> param){ this.credenciais = param; }
    
    
}
