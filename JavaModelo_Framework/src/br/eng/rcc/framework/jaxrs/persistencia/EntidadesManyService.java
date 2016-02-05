package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/persistencia/many")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesManyService {

  @Inject
  private EntityManager em;
  @Inject
  private ClassCache cache;
  @Inject
  private SegurancaServico checker;
  
  /**
    * Para que este objeto possa fazer o seu trabalho, é obrigatório um 
    * {@link EntityManager EntityManager} para acessar o banco.
    */
  @PostConstruct
  public void postConstruct(){
      if( em == null ){
          String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
          Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
          throw new MsgException(msg);
      }
  } 
  
  
  @GET
  @Path("/")
  @Transactional
  public Object buscarVariasEntidades(List<?> objs) {

    return new JsonResponse(false, null);
  }

  @POST
  @Path("/")
  @Transactional
  public Object criarVariasEntidades(List<?> objs) {

    return new JsonResponse(false, null);
  }

  @PUT
  @Path("/")
  @Transactional
  public Object editarVariasEntidades(List<?> objs) {

    return new JsonResponse(false, null);
  }

  @DELETE
  @Path("/")
  @Transactional
  public Object deletarVariasEntidades(List<?> objs) {

    return new JsonResponse(false, null);
  }

}
