package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/persistencia/um/id/{entidade}")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesUmIdService {

  @Inject
  protected EntityManager em;
  @Inject
  protected ClassCache cache;
  @Inject
  protected SegurancaServico checker;
  @Inject
  protected EntidadesService entService;
    
  
  /**
   * Para que este objeto possa fazer o seu trabalho, é obrigatório um
   * {@link EntityManager EntityManager} para acessar o banco.
   */
  @PostConstruct
  public void postConstruct() {
    if (em == null) {
      String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
      throw new MsgException(JsonResponse.ERROR_DESCONHECIDO,null,msg);
    }
  }
  
  //=====================================================================
  
  @GET @Path("/tipo")
  public JsonResponse tipo(
          @PathParam("entidade") String entidade
    ){
    Class<?> klass = cache.get(entidade, em);
    if( klass == null ){
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
    }
    return new JsonResponse(true, entService.tipo(klass), "Tipo um ID");
  }
  
  
  @POST
  @Path("/buscar")
  @Transactional
  public JsonResponse buscar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx,
          JsonNode node
  ) {
    // O JSON deve ser um Array, com as entidades dentro
    if( node == null || !node.isArray() ){
      return new JsonResponse(false,"A mensagem deve ser um Array JSON.");
    }
    
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( ctx.getPath(), cache );
    
    // Pegando os ids da classe
    List<String> ids = PersistenciaUtils.getIds(em, info.classe);
    if (ids == null || ids.isEmpty()) {
      return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
    }
    
    List resposta = new ArrayList<>();
    
    for(JsonNode json : node){
      if( json == null || !json.isObject() ) continue;
      info.query = new String[ ids.size() ][];
      int i = 0;
      for( String idAttr : ids ){
        String[] idS = idAttr.split("\\.");
        JsonNode prop = json;
        for( String s : idS ) prop = prop.get(s);
        if( prop == null /* ... comp do tipo do ID */ ) continue;
        info.query[i++] = new String[]{ idAttr, "=", prop.asText(), "&" };
      }
      
      List lista = entService.buscar(info);
      for( Object x : lista ) resposta.add(x);
    }
    
    return new JsonResponse(true, resposta, "Buscar um ID");
  }
  
  
  // ---  Sem método "criar": nenhuma entidade por ser criada pelo ID
  @POST
  @Path("/")
  @Transactional
  public JsonResponse criar(){
    return new JsonResponse(false,"Não é possível criar entidades a partir do ID!");
  }
  
  
  @PUT
  @Path("/")
  @Transactional
  public JsonResponse editar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx,
          JsonNode node
  ) {
    // O JSON deve ser um Array, com as entidades dentro
    if( node == null || !node.isArray() ){
      return new JsonResponse(false,"A mensagem deve ser um Array JSON.");
    }
    
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( ctx.getPath(), cache );
    // Pegando os ids da classe
    List<String> ids = PersistenciaUtils.getIds(em, info.classe);
    if (ids == null || ids.isEmpty()) {
      return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
    }
    
    int upsTotal = 0;
    for(JsonNode json : node){
      if( json == null || !json.isObject() ) continue;
      info.query = new String[ ids.size() ][];
      int i = 0;
      for( String idAttr : ids ){
        String[] idS = idAttr.split("\\.");
        JsonNode prop = json;
        for( String s : idS ) prop = prop.get(s);
        if( prop == null /* ... comp do tipo do ID */ ) continue;
        info.query[i++] = new String[]{ idAttr, "=", prop.asText(), "&" };
      }
      
      upsTotal += entService.editar(info, json);
    }
    
    return new JsonResponse(true, upsTotal, "Editar um ID");
  }
  
  
  @DELETE
  @Path("/")
  @Transactional
  public JsonResponse deletar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx,
          JsonNode node
  ){
    // O JSON deve ser um Array, com as entidades dentro
    if( node == null || !node.isArray() ){
      return new JsonResponse(false,"A mensagem deve ser um Array JSON.");
    }
    
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( ctx.getPath(), cache );
    // Pegando os ids da classe
    List<String> ids = PersistenciaUtils.getIds(em, info.classe);
    if (ids == null || ids.isEmpty()) {
      return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
    }
    
    int upsTotal = 0;
    for(JsonNode json : node){
      if( json == null || !json.isObject() ) continue;
      info.query = new String[ ids.size() ][];
      int i = 0;
      for( String idAttr : ids ){
        String[] idS = idAttr.split("\\.");
        JsonNode prop = json;
        for( String s : idS ) prop = prop.get(s);
        if( prop == null /* ... comp do tipo do ID */ ) continue;
        info.query[i++] = new String[]{ idAttr, "=", prop.asText(), "&" };
      }
      
      upsTotal += entService.deletar(info);
    }
    
    return new JsonResponse(true, upsTotal, "Deletar um ID");
  }
  
  
  
  
  
}
