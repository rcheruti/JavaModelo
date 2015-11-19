
package br.eng.rcc.seguranca.entidades;

import br.eng.rcc.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name="seg_credencial")
@Seguranca(delete = false, select = false, insert = false, update = false)
public class Credencial implements Serializable{
    
    @Id
    private int id;
    
    private String login;
    private String senha;
    private boolean renovarSenha;
    private short erros;
    private boolean bloqueado;
    
    
    
    @OneToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "seg_fk_credencial_grupo")
    private Set<Grupo> grupos;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "seg_fk_credencial_permissao")
    private Set<Permissao> permissoes;
    
    
    //========================================================================
    
    @PostLoad
    private void _postLoad(){
        this.senha = null;
    }
    
    
    //========================================================================
    
    public Credencial clone(){
        Credencial copia = new Credencial();
        copia.id = this.getId();
        copia.login = this.getLogin();
        copia.senha = null;
        copia.usuario = null;
        
        Set<Grupo> cGrupos = this.getGrupos();
        if( cGrupos == null ) copia.grupos = new HashSet<>(0);
        else{
            copia.grupos = new HashSet<>( cGrupos.size() );
            for( Grupo g : cGrupos ){
                copia.grupos.add( g.clone() );
            }
        }
        
        Set<Permissao> cPerms = this.getPermissoes();
        if( cPerms == null ) copia.permissoes = new HashSet<>(0);
        else{
            copia.permissoes = new HashSet<>( cPerms.size() );
            for( Permissao p : cPerms ){
                copia.permissoes.add( p.clone() );
            }
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
        if( !( objT instanceof Credencial ) ) return false;
        Credencial obj = (Credencial) objT;
        if( this.getId() == obj.getId()
                ) return true;
        return false;
    }
    
    //========================================================================
    
    public boolean hasGrupo(String nome){
        if( nome == null ) return false;
        for( Grupo g : grupos ){
            if( nome.equals( g.getChave() ) ) return true;
        }
        return false;
    }
    public boolean hasPermissao(String perm){
        if( perm == null ) return false;
        if( permissoes != null ){
            for( Permissao p : permissoes ){
                if( perm.equals( p.getNome() ) ) return true;
            }
        }
        if( grupos != null ){
            for( Grupo g : grupos ){
                if( g.hasPermissao(perm) ) return true;
            }
        }
        return false;
    }
    
    
    //========================================================================
    
    public int getId(){ return this.id; }
    public void setId(int param){ this.id = param; }
    
    public String getLogin(){ return this.login; }
    public void setLogin(String param){ this.login = param; }
    
    public String getSenha(){ return this.senha; }
    public void setSenha(String param){ this.senha = param; }
    
    public boolean getBloqueado(){ return this.bloqueado; }
    public void setBloqueado(boolean param){ this.bloqueado = param; }
    
    public short getErros(){ return this.erros; }
    public void setErros(short param){ this.erros = param; }
    
    public boolean getRenovarSenha(){ return this.renovarSenha; }
    public void setRenovarSenha(boolean param){ this.renovarSenha = param; }
    
    public Usuario getUsuario(){ return this.usuario; }
    public void setUsuario(Usuario param){ this.usuario = param; }
    
    public Set<Grupo> getGrupos(){ return this.grupos; }
    public void setGrupos(Set<Grupo> param){ this.grupos = param; }
    
    public Set<Permissao> getPermissoes(){ return this.permissoes; }
    public void setPermissoes(Set<Permissao> param){ this.permissoes = param; }
    
    
}
