
package br.eng.rcc.framework.seguranca.servicos;

import br.eng.rcc.framework.Configuracoes;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.interfaces.UsuarioInterface;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Esta classe fornece funções para acessar o usuário que está disponível na
 * sessão atual do servidor (em relação a requisição que está correndo).
 * 
 * 
 * 
 * @author rcheruti
 */
@RequestScoped
public class UsuarioService {
  
  @Inject
  private EntityManager em;
  @Inject
  private HttpServletRequest req;
  
  
  //=======================  Utilitarios  =========================
  
  public UsuarioInterface getUsuario(){
    HttpSession session = req.getSession(false);
    if( session != null ){
      try{
        UsuarioInterface u = (UsuarioInterface) session.getAttribute(UsuarioInterface.usuarioKey);
        return u;
      }catch(ClassCastException ex){
        return null;
      }
    }
    return null;
  }
  
  public String criptografar(String s){
    return DigestUtils.sha1Hex(s);
  }
  public void checkLogin(){
    HttpSession session = req.getSession(false);
    if( session != null ){
      Object usuario = session.getAttribute(UsuarioInterface.usuarioKey);
      if( usuario != null ){
        return;
      }
    }
    
    for( Cookie cookie : req.getCookies() ){
      if( !cookie.getName().equals( Configuracoes.loginCookieName ) )continue;
      //em.find( ChaveAcesso.class, cookie.getValue() );
      return;
    }
    throw new MsgException("O usuário não está logado");
  }
  
}
