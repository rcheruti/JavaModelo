
package br.eng.rcc.seguranca.jaxrs;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.seguranca.entidades.Credencial;
import br.eng.rcc.seguranca.entidades.Usuario;
import br.eng.rcc.seguranca.filtros.SegurancaFiltro;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;

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
    @Inject
    private UsuarioService uService;
    
    /**
     * Este método verifica as credenciais do usuário.
     * Caso estejam corretas (existam na base de dados), este método iniciará a
     * sessão do usuário e colocará o objeto "Usuário" deste usuário quardado
     * como um atributo da sessão deste usuário.
     * 
     * A chave para buscar o objeto "Usuário" esta guardada na classe {@link br.eng.rcc.seguranca.filtros.SegurancaFiltro SegurancaFiltro}.
     * 
     * @param login "Login" do usuário que está entrando no sistema.
     * @param senha "Senha" do usuário que esta entrando no sistema.
     * @return 
     */
    @POST @Path("/login")
    public Object login(
                @FormParam("login") String login, 
                @FormParam("senha") String senha){
        if( login == null || senha == null ){
            throw new MsgException("Informe o usuário e a senha para fazer a autenticação");
        }
        
        List<Credencial> credenciais = em.createNamedQuery("Credencial.login")
                .setParameter("login", login)
                .setParameter("senha", uService.criptografar(senha) )
                .getResultList();
        
        
        if( credenciais.size() != 1 ){
            throw new MsgException("Este conjunto de login e senha não existe.");
        }
        
        Credencial c = credenciais.get(0);
        Usuario u = c.getUsuario();
        
        HttpSession session = req.getSession();
        session.setAttribute(SegurancaFiltro.usuarioKey, u ); 
        
        return new JsonResponse(true,"Logado");
    }
    
    @POST @GET @PUT @DELETE @Path("/logout")
    public Object logout(){
        
        HttpSession session = req.getSession(false);
        if( session != null ){
            session.removeAttribute(SegurancaFiltro.usuarioKey);
            session.invalidate();
        }
        
        return new JsonResponse(true,"Logout");
    }
    
    public void checkLogin(){
        HttpSession session = req.getSession(false);
        if( session != null ){
            Object usuario = session.getAttribute(SegurancaFiltro.usuarioKey);
            if( usuario != null ){
                return;
            }
        }
        throw new MsgException("O usuário não está logado");
    }
}
