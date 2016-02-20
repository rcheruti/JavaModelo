package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  @Inject
  private EntidadesService entidadesService;
  
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
  
  /**
  * Este método retorna os tipos das entidades passadas no vetor JSON.
  * 
  * @param entidades Um vetor JSON com os nomes das entidades: ["Carro","Pessoa",...]
  * @return {@link JsonResponse}
  */
  @GET
  @Path("/tipo")
  @Transactional
  public JsonResponse tipo(List<String> entidades) {
    checkNull(entidades);
    Map<String, Object> resposta = new HashMap<>( entidades.size() + 2, 1 );
    for(String ent : entidades){
      JsonResponse resp = entidadesService.tipo(ent);
      resposta.put( ent, resp.data );
    }
    return new JsonResponse(true, resposta, "Many Tipo");
  }
  
  @GET
  @Path("/")
  @Transactional
  public JsonResponse buscar(JsonNode node) {

    return new JsonResponse(false, null);
  }

  @POST
  @Path("/")
  @Transactional
  public JsonResponse criar(JsonNode node) {

    return new JsonResponse(false, null);
  }

  @PUT
  @Path("/")
  @Transactional
  public JsonResponse editar(JsonNode node) {

    return new JsonResponse(false, null);
  }

  @DELETE
  @Path("/")
  @Transactional
  public JsonResponse deletar(JsonNode node) {

    return new JsonResponse(false, null);
  }
  
  
  //===========================================================================
  private void checkNull(Object obj){
    if( obj == null ) throw new MsgException(false,"Para usar estes serviços é necessário enviar uma mensagem JSON para o servidor");
  }
  
  
  
}
