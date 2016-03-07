package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/persistencia/muitos/id")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesMuitosIdService {

  @Inject
  private EntityManager em;
  @Inject
  private ClassCache cache;
  @Inject
  private EntidadesService entService;

  /**
   * Para que este objeto possa fazer o seu trabalho, é obrigatório um
   * {@link EntityManager EntityManager} para acessar o banco.
   */
  @PostConstruct
  public void postConstruct() {
    if (em == null) {
      String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
      throw new MsgException(JsonResponse.ERROR_DESCONHECIDO, null, msg);
    }
  }

  /**
  * Este método retorna os tipos das entidades passadas no vetor JSON.
  * 
  * @param json Um vetor JSON com os nomes das entidades: ["Carro","Pessoa",...]
  * @return {@link JsonResponse}
  */
  @POST
  @Path("/tipo")
  public JsonResponse tipo(JsonNode json) {
    if( json == null || !json.isArray() ) throw new MsgException(JsonResponse.ERROR_EXCECAO,null,
            "Para usar estes serviços é necessário enviar uma mensagem JSON para o servidor");
    Map<String, Object> resposta = new HashMap<>( json.size() + 2, 1 );
    for(JsonNode node : json){
      Map resp = entService.tipo( cache.get( node.asText() ) );
      resposta.put( node.asText(), resp );
    }
    return new JsonResponse(true, resposta, "Tipo muitos ID");
  }
  
  
  @POST
  @Path("/buscar")
  @Transactional
  public JsonResponse buscar( JsonNode json ){
    checkNull(json);
    List<PersistenciaUtils.BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    Map<String, List> resposta = new HashMap<>( json.size() + 2, 1 );
    for( PersistenciaUtils.BuscaInfo info : buscas ){
      List lista = resposta.get(info.entidade);
      if( lista == null ){
        lista = new ArrayList<>();
        resposta.put(info.entidade, lista);
      }
      
      List<String> ids = PersistenciaUtils.getIds(em, info.classe);
      if (ids == null || ids.isEmpty()) {
        return new JsonResponse(false, String
                .format("Não encontramos os campos de Id dessa classe: '%s'", info.entidade));
      }
      
      for( JsonNode node : (JsonNode)info.data ){
        if( node == null || !node.isObject() ) continue;
        info.query = new String[ ids.size() ][];
        int i = 0;
        for( String idAttr : ids ){
          String[] idS = idAttr.split("\\.");
          JsonNode prop = node;
          for( String s : idS ) prop = prop.get(s);
          if( prop == null /* ... comp do tipo do ID */ ) continue;
          info.query[i++] = new String[]{ idAttr, "=", prop.asText(), "&" };
        }

        List listaBusca = entService.buscar(info);
        for( Object x : listaBusca ) lista.add(x);
      }
    }
    return new JsonResponse(true, resposta, "Buscar muitos ID");
  }
  
  
  @POST
  @Path("/")
  @Transactional
  public JsonResponse criar(){
    return new JsonResponse(false,"Não é possível criar entidades a partir do ID!");
  }
  
  
  @PUT
  @Path("/")
  @Transactional
  public JsonResponse editar(JsonNode json){
    checkNull(json);
    List<PersistenciaUtils.BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    Map<String, Integer> resposta = new HashMap<>( json.size() + 2, 1 );
    for( PersistenciaUtils.BuscaInfo info : buscas ){
      Integer val = resposta.get(info.entidade);
      if( val == null ){
        val = 0;
      }
      
      List<String> ids = PersistenciaUtils.getIds(em, info.classe);
      if (ids == null || ids.isEmpty()) {
        return new JsonResponse(false, String
                .format("Não encontramos os campos de Id dessa classe: '%s'", info.entidade));
      }
      
      for( JsonNode node : (JsonNode)info.data ){
        if( node == null || !node.isObject() ) continue;
        info.query = new String[ ids.size() ][];
        int i = 0;
        for( String idAttr : ids ){
          String[] idS = idAttr.split("\\.");
          JsonNode prop = node;
          for( String s : idS ) prop = prop.get(s);
          if( prop == null /* ... comp do tipo do ID */ ) continue;
          info.query[i++] = new String[]{ idAttr, "=", prop.asText(), "&" };
        }

        val += entService.editar(info, node);
        resposta.put(info.entidade, val);
      }
    }
    return new JsonResponse(true, resposta, "Editar muitos ID");
  }
  
  
  @DELETE
  @Path("/")
  @Transactional
  public JsonResponse deletar(JsonNode json){
    checkNull(json);
    List<PersistenciaUtils.BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    Map<String, Integer> resposta = new HashMap<>( json.size() + 2, 1 );
    for( PersistenciaUtils.BuscaInfo info : buscas ){
      Integer val = resposta.get(info.entidade);
      if( val == null ){
        val = 0;
      }
      
      List<String> ids = PersistenciaUtils.getIds(em, info.classe);
      if (ids == null || ids.isEmpty()) {
        return new JsonResponse(false, String
                .format("Não encontramos os campos de Id dessa classe: '%s'", info.entidade));
      }
      
      for( JsonNode node : (JsonNode)info.data ){
        if( node == null || !node.isObject() ) continue;
        info.query = new String[ ids.size() ][];
        int i = 0;
        for( String idAttr : ids ){
          String[] idS = idAttr.split("\\.");
          JsonNode prop = node;
          for( String s : idS ) prop = prop.get(s);
          if( prop == null /* ... comp do tipo do ID */ ) continue;
          info.query[i++] = new String[]{ idAttr, "=", prop.asText(), "&" };
        }

        val += entService.deletar(info);
        resposta.put(info.entidade, val);
      }
    }
    return new JsonResponse(true, resposta, "Deletar muitos ID");
  }
  
  
  
  //===========================================================================
  private void checkNull(JsonNode json){
    if( json == null || !json.isArray() ) throw new MsgException(JsonResponse.ERROR_EXCECAO,null,
            "Para usar estes serviços é necessário enviar uma mensagem JSON para o servidor");
  }
  
  
}
