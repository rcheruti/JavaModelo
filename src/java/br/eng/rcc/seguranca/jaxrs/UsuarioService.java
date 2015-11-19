
package br.eng.rcc.seguranca.jaxrs;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.seguranca.entidades.Usuario;
import br.eng.rcc.seguranca.filtros.SegurancaFiltro;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Esta classe fornece funções para acessar o usuário que está disponível na
 * sessão atual do servidor (em relação a requisição que está correndo).
 * 
 * 
 * 
 * @author rcheruti
 */
@Path("/Usuario")
@RequestScoped
public class UsuarioService {
    
    @Inject
    private EntityManager em;
    @Inject
    private HttpServletRequest req;
    
    
        
    /**
     * Retorna um clone do usuário atual, mas anulando o atributo "credencial".
     * 
     * @return {@link br.eng.rcc.seguranca.entidades.Usuario Usuario}
     */
    @GET @Path("/")
    public Object carregarUsuario(){
        Usuario u = getUsuario().clone();
        u.setCredencial(null);
        return new JsonResponse(true,u, "Usuário atual");
    }
    
    
    
    //=======================  Utilitarios  =========================
    public String criptografar(String s){
        return DigestUtils.sha1Hex(s);
    }
    public Usuario getUsuario(){
        HttpSession session = req.getSession(false);
        if( session != null ){
            try{
                Usuario u = (Usuario) session.getAttribute(SegurancaFiltro.usuarioKey);
                return u;
            }catch(ClassCastException ex){
                return null;
            }
        }
        return null;
    }
    
}
