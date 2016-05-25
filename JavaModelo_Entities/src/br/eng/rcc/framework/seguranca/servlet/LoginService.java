
package br.eng.rcc.framework.seguranca.servlet;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.interfaces.IUsuario;
import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.entidades.ChaveAcesso;
import br.eng.rcc.framework.seguranca.entidades.Credencial;
import br.eng.rcc.framework.seguranca.entidades.Grupo;
import br.eng.rcc.framework.seguranca.entidades.SegUsuario;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

@WebServlet("/seguranca/*")
public class LoginService extends HttpServlet{
  
  
  @Inject 
  private EntityManager em;
  @Inject
  private UsuarioServico uService;
  
  private ObjectMapper mapper;
  
  
  //====================================================================
  
  /**
   * Retorna um clone do usuário atual.
   * Este serviço espera que o método "clone" já se preocupe com as questões de segurança
   * (ex.: não permitindo que o clone tenha informações sobre senhas, chaves, e informações delicadas
   * sobre o usuário)
   * 
   * @return {@link br.eng.rcc.framework.seguranca.entidades.SegUsuario Usuario}
   */
  public Object carregarUsuario(){
    IUsuario u = uService.getUsuario();
    if( u == null ) throw new MsgException("Nenhum usuário está logado");
    return new JsonResponse(true, u.clone() , "Usuário atual");
  }
  
  public Object loginJson( HttpServletRequest req, HttpServletResponse resp, 
            JsonNode node ){
    if( node == null || node.get("login") == null || node.get("senha") == null ) return loginDB(req,resp,null,null);
    return loginDB( req, resp, node.get("login").textValue() , node.get("senha").textValue() );
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
  @Transactional
  public Object loginDB( HttpServletRequest req, HttpServletResponse resp, 
            String login,  String senha ){
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
        Cookie cook = new Cookie(Configuracoes.getInstance().loginCookieName(), cookieValue);
        cook.setPath("/");
        resp.addCookie( cook );
      }
    }
    
    return new JsonResponse(true,"Logado");
  }
  
  @Transactional
  public Object logout(HttpServletRequest req, HttpServletResponse resp){
    
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
        if( !cookie.getName().equals( Configuracoes.getInstance().loginCookieName() ) ) continue;
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
  
  //====================================================================
  
  @Override
  public void init(){
    mapper = new JacksonObjectMapperContextResolver().getContext(this.getClass());
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException{
    String path = req.getPathInfo();
    System.out.printf("---  EM: %s \n", em);
    System.out.printf("---  pathInfo: %s \n", path);
    
    Object resposta = null;
    
    switch(path){
      case "/usuario":
        resposta = this.carregarUsuario();
        break;
      case "/login":
        String contType = req.getContentType();
        if( contType != null && contType.contains("application/json") ){
          resposta = this.loginJson(req, resp, 
                  mapper.readValue(req.getInputStream(), JsonNode.class) );
        }else{
          resposta = this.loginDB(req, resp, 
                  req.getParameter("login"), req.getParameter("senha"));
        }
        break;
      case "/logout":
        resposta = this.logout(req, resp);
        break;
      default:
        throw new MsgException(String.format("---  Essa URL não existe: '%s' \n", 
                req.getRequestURI()));
    }
    
    mapper.writeValue(resp.getWriter(), resposta);
    
  }
  
  //---
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException{
    doPost(req,resp);
  }
  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException{
    doPost(req,resp);
  }
  @Override
  public void doDelete(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException{
    doPost(req,resp);
  }
  
  
}
