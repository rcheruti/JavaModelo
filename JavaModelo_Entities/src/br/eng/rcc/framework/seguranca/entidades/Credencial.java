
package br.eng.rcc.framework.seguranca.entidades;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

@Entity
@Table(name="seg_credencial")
@Seguranca(delete = false, select = false, insert = false, update = false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Credencial implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    
    @Column(nullable = false, unique = true, length = 255 )
    protected String login;
    
    @Column(nullable = false, length = 1024 )
    protected byte[] senha;
    
    @Column(nullable = false)
    protected boolean renovarSenha;
    
    @Column(nullable = false)
    protected short erros;
    
    @Column(nullable = false)
    protected boolean bloqueado;
    
    
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "seg_fk_credencial_grupo")
    protected Set<Grupo> grupos;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "seg_fk_credencial_permissao")
    protected Set<Permissao> permissoes;
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "credencial", cascade = CascadeType.REMOVE)
    protected List<ChaveAcesso> chaveAcesso;
    
    @OneToOne(fetch = FetchType.EAGER)
    protected SegUsuario usuario;
    
    //========================================================================
    
    @Override
    public Credencial clone(){
        Credencial copia = new Credencial();
        copia.id = this.getId();
        copia.login = this.getLogin();
        
        copia.senha = null; // !! Nunca enviar as senhas!
        copia.chaveAcesso = null; // !! Nunca enviar as senhas!
        //copia.usuario = null;
        
        //Usuario usuario = this.getUsuario();
        //copia.usuario = (usuario!=null)? usuario.clone() : null;
        
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
    
    public byte[] getSenha(){ return this.senha; }
    public void setSenha(byte[] param){ this.senha = param; }
    
    public boolean getBloqueado(){ return this.bloqueado; }
    public void setBloqueado(boolean param){ this.bloqueado = param; }
    
    public short getErros(){ return this.erros; }
    public void setErros(short param){ this.erros = param; }
    
    public boolean getRenovarSenha(){ return this.renovarSenha; }
    public void setRenovarSenha(boolean param){ this.renovarSenha = param; }
    
    public SegUsuario getUsuario(){ return this.usuario; }
    public void setUsuario(SegUsuario param){ this.usuario = param; }
    
    public Set<Grupo> getGrupos(){ return this.grupos; }
    public void setGrupos(Set<Grupo> param){ this.grupos = param; }
    
    public Set<Permissao> getPermissoes(){ return this.permissoes; }
    public void setPermissoes(Set<Permissao> param){ this.permissoes = param; }
    
    public List<ChaveAcesso> getChaveAcesso(){ return this.chaveAcesso; }
    public void setChaveAcesso(List<ChaveAcesso> param){ this.chaveAcesso = param; }
    
}
