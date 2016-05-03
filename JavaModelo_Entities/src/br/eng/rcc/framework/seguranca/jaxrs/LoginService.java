
package br.eng.rcc.framework.seguranca.jaxrs;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.entidades.ChaveAcesso;
import br.eng.rcc.framework.seguranca.entidades.Credencial;
import br.eng.rcc.framework.seguranca.entidades.SegUsuario;
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
import br.eng.rcc.framework.interfaces.IUsuario;
import br.eng.rcc.framework.seguranca.entidades.Grupo;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Objetos desta classe são usados para "logar" e "deslogar" usuários no sistema.
 * Estes recursos estão disponíveis através da CDI.
 * 
 * @author rcheruti
 */
@Path("/seguranca")
@RequestScoped
public class LoginService {
  
  @Inject 
  private EntityManager em;
  @Inject
  private HttpServletRequest req;
  @Context
  private HttpServletResponse resp;
  @Inject
  private UsuarioServico uService;
  
   /**
   * Retorna um clone do usuário atual.
   * Este serviço espera que o método "clone" já se preocupe com as questões de segurança
   * (ex.: não permitindo que o clone tenha informações sobre senhas, chaves, e informações delicadas
   * sobre o usuário)
   * 
   * @return {@link br.eng.rcc.framework.seguranca.entidades.SegUsuario Usuario}
   */
  @GET @Path("/usuario")
  public Object carregarUsuario(){
    IUsuario u = uService.getUsuario();
    if( u == null ) throw new MsgException("Nenhum usuário está logado");
    return new JsonResponse(true, u.clone() , "Usuário atual");
  }
  
  @POST @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Object loginJson( JsonNode node ){
    if( node == null || node.get("login") == null || node.get("senha") == null ) return loginDB(null,null);
    return loginDB( node.get("login").textValue() , node.get("senha").textValue() );
  }
  
  /**
   * Este método verifica as credenciais do usuário.
   * Caso estejam corretas (existam na base de dados), este método iniciará a
   * sessão do usuário e colocará o objeto "IUsuário" deste usuário guardado
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
    if( !uService.checkLogin( true ) ){
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
      SegUsuario u = c.getUsuario();
      
      if( c.getBloqueado() ){
        throw new MsgException("O seu usuário está bloqueado!");
      }
      
      // temos que carregar os atrasados!:
      c.getPermissoes().size();
      if( c.getGrupos() != null ) for(Grupo g : c.getGrupos()) g.getPermissoes().size();
      
      HttpSession session = req.getSession();
      session.setAttribute(IUsuario.USUARIO_KEY, u.clone() ); 
      
      em.clear();
      String cookieValue = createChaveAcesso();
      if( cookieValue != null ){
        Cookie cook = new Cookie(Configuracoes.loginCookieName, cookieValue);
        cook.setPath("/");
        resp.addCookie( cook );
      }
    }
    
    return new JsonResponse(true,"Logado");
  }
  
  
  
  @POST @GET @PUT @DELETE @Path("/logout")
  @Transactional
  public Object logout(){
    
    SegUsuario u = null;
    HttpSession session = req.getSession(false);
    if( session != null ){
      u = (SegUsuario)session.getAttribute(IUsuario.USUARIO_KEY);
      session.removeAttribute(IUsuario.USUARIO_KEY);
      session.invalidate();
    }
    if( u != null ){
      em.createQuery("DELETE FROM ChaveAcesso x WHERE x.credencial.id = :K " )
        .setParameter("K", u.getCredencial().getId() )
        .executeUpdate();
    }else{
      if( req.getCookies() != null ) for( Cookie cookie : req.getCookies() ){
        if( !cookie.getName().equals( Configuracoes.loginCookieName ) ) continue;
        em.createQuery("DELETE FROM ChaveAcesso x WHERE x.chave = :chave")
          .setParameter("chave", cookie.getValue() )
          .executeUpdate();
      }
    }

    return new JsonResponse(true,"Logout");
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
        try{
          criarCom = new String( MessageDigest.getInstance("SHA-512")
                  .digest( tempString.getBytes() ) );
          ChaveAcesso chave = new ChaveAcesso();
          chave.setChave(criarCom);
          chave.setCredencial( ((SegUsuario)uService.getUsuario()).getCredencial() );
          em.persist(chave);
          break;
        }catch(NoSuchAlgorithmException ex){
          ex.printStackTrace();
        }
      }
    }
    return criarCom ;
  }
  
}
