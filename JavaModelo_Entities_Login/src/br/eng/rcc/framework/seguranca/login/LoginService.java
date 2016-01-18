
package br.eng.rcc.framework.seguranca.login;

import br.eng.rcc.framework.Configuracoes;
import br.eng.rcc.framework.seguranca.servicos.UsuarioService;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.entidades.ChaveAcesso;
import br.eng.rcc.framework.seguranca.entidades.Credencial;
import br.eng.rcc.framework.seguranca.entidades.Usuario;
import br.eng.rcc.framework.seguranca.interfaces.UsuarioInterface;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Objetos desta classe são usados para "logar" e "deslogar" usuários no sistema.
 * Estes recursos estão disponíveis através da CDI.
 * 
 * @author rcheruti
 */
@Path("/seguranca")
@RequestScoped
public class LoginService {
  
  // 
  private String cookieName = "";
  
  @Inject 
  private EntityManager em;
  @Inject
  private HttpServletRequest req;
  @Context
  private HttpServletResponse resp;
  @Inject
  private UsuarioService uService;
  
   /**
   * Retorna um clone do usuário atual, mas anulando o atributo "credencial".
   * 
   * @return {@link br.eng.rcc.framework.seguranca.entidades.Usuario Usuario}
   */
  @GET @Path("/")
  public Object carregarUsuario(){
    Usuario u = ((Usuario) uService.getUsuario()).clone();
    u.setCredencial(null);
    return new JsonResponse(true,u, "Usuário atual");
  }
  
  @POST @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Object loginJson( JsonNode node ){
    if( node.get("login") == null || node.get("senha") == null ) return loginDB(null,null);
    return loginDB( node.get("login").textValue() , node.get("senha").textValue() );
  }
  
  /**
   * Este método verifica as credenciais do usuário.
   * Caso estejam corretas (existam na base de dados), este método iniciará a
   * sessão do usuário e colocará o objeto "Usuário" deste usuário quardado
   * como um atributo da sessão deste usuário.
   * 
   * A chave para buscar o objeto "Usuário" esta guardada na classe {@link br.eng.rcc.framework.seguranca.filtros.SegurancaFiltro SegurancaFiltro}.
   * 
   * @param login "Login" do usuário que está entrando no sistema.
   * @param senha "Senha" do usuário que esta entrando no sistema.
   * @return 
   */
  @POST @Path("/login")
  @Transactional
  public Object loginDB(
      @FormParam("login") String login, 
      @FormParam("senha") String senha ){
    try{
      checkLogin();
    }catch(MsgException ex){
      if( login == null || senha == null ){
        throw new MsgException("Informe o usuário e a senha para fazer a autenticação");
      }
      List<Credencial> credenciais = em
          .createQuery("SELECT x FROM Credencial x WHERE x.login = :login AND x.senha = :senha")
          .setParameter("login", login)
          .setParameter("senha", uService.criptografar(senha) )
          .getResultList();


      if( credenciais.size() != 1 ){
        throw new MsgException("Este conjunto de login e senha não existe.");
      }

      Credencial c = credenciais.get(0);
      em.detach(c);
      Usuario u = c.getUsuario();

      HttpSession session = req.getSession();
      session.setAttribute(UsuarioInterface.usuarioKey, u ); 

      String cookieValue = createChaveAcesso();
      if( cookieValue != null ){
        resp.addCookie( new Cookie(Configuracoes.loginCookieName, cookieValue) );
      }
    }
    
    return new JsonResponse(true,"Logado");
  }
  
  @POST @GET @PUT @DELETE @Path("/logout")
  public Object logout(){

    HttpSession session = req.getSession(false);
    if( session != null ){
      session.removeAttribute(UsuarioInterface.usuarioKey);
      session.invalidate();
    }

    return new JsonResponse(true,"Logout");
  }
  
  
  public Usuario loginCookie( Cookie[] cookies ){
    for( Cookie cookie : cookies ){
      Usuario u = this.loginCookie(cookie);
      if( u != null ) return u;
    }
    return null;
  }
  public Usuario loginCookie( Cookie cookie ){
    if( !cookie.getName().equals( Configuracoes.loginCookieName ) ) return null;
    ChaveAcesso ca = em.find( ChaveAcesso.class, cookie.getValue() );
    if( ca == null ) return null;
    return ca.getUsuario() ;
  }
  
  @Transactional
  public String createChaveAcesso(){
    byte tentativas = 0;
    String criarCom = null;
    while( tentativas++ < 50 ){
      long tempo = System.currentTimeMillis();
      String tempString = Long.toString(tempo);
      List lista = em.createQuery("SELECT COUNT(x) FROM ChaveAcesso x WHERE x.chave = :chave ")
        .setParameter("chave", tempString)
        .getResultList();
      if( (long)lista.get(0) < 1 ){
        criarCom = DigestUtils.sha1Hex( tempString );
        ChaveAcesso chave = new ChaveAcesso();
        chave.setChave(criarCom);
        chave.setUsuario( (Usuario)uService.getUsuario() );
        em.persist(chave);
        break;
      }
    }
    if( criarCom == null ){
      System.out.println("\n\n  ---  Foram feitas muitas tentativas antes de acharmos um Hash para o usuário!");
      return null;
    }
    System.out.println("\n\n  --- Hash do Usuario: "+ criarCom );
    return criarCom ;
  }
  
  
  /*
    Essa função é temporária, uma interface precisa ser criada (e organizada) para isso
    ir para o UserService no jar Core.
    O problema esta na compilação do jar com referência a classe ChaveAcesso
  */
  @Deprecated
  public void checkLogin(){
    HttpSession session = req.getSession(false);
    if( session != null ){
      Object usuario = session.getAttribute(UsuarioInterface.usuarioKey);
      if( usuario != null ){
        return;
      }
    }
    
    for( Cookie cookie : req.getCookies() ){
      if( !cookie.getName().equals( Configuracoes.loginCookieName ) ) continue;
      em.find( ChaveAcesso.class, cookie.getValue() );
      return;
    }
    throw new MsgException("O usuário não está logado");
  }
  
  
}
