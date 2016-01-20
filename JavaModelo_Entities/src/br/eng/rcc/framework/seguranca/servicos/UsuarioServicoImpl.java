
package br.eng.rcc.framework.seguranca.servicos;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.MsgException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.eng.rcc.framework.interfaces.IUsuario;
import br.eng.rcc.framework.seguranca.entidades.ChaveAcesso;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.servlet.http.Cookie;

/**
 * Esta classe fornece funções para acessar o usuário que está disponível na
 * sessão atual do servidor (em relação a requisição que está correndo).
 * 
 * 
 */
@Specializes
@ApplicationScoped
public class UsuarioServicoImpl extends UsuarioServico{
  
  @Inject
  protected EntityManager em;
  
  //=======================  Utilitarios  =========================
  
  /*
  public byte[] criptografar(String str){
    return DigestUtils.getDigest( Configuracoes.encriptionScript ).digest( str.getBytes() );
  }
  */
  
  @Override
  public boolean checkLogin(boolean returnOrThrow){
    IUsuario usuario = getUsuario();
    if( usuario != null ) return true;
    
    if( this.req.getCookies() != null ) for( Cookie cookie : this.req.getCookies() ){
      if( !cookie.getName().equals( Configuracoes.loginCookieName ) )continue;
      List<ChaveAcesso> oo = em.createQuery("SELECT x FROM ChaveAcesso x WHERE x.chave = :chave")
              .setParameter("chave", cookie.getValue())
              .setMaxResults(1)
              .getResultList();
      if( oo.size() > 0 ){
        this.req.getSession().setAttribute( IUsuario.USUARIO_KEY , oo.get(0).getCredencial().getUsuario().clone() );
        return true;
      }
    }
    
    if( !returnOrThrow ) throw new MsgException("O usuário não está logado");
    return false;
  }
  
  
  @Override
  public boolean checkLogin(){
    return checkLogin(false);
  }
  
}
