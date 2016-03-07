package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
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

@Path("/persistencia/muitos")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesMuitosService {

  @Inject
  private EntityManager em;
  @Inject
  private ClassCache cache;
  @Inject
  private SegurancaServico checker;
  @Inject
  private EntidadesService entService;
  
  /**
    * Para que este objeto possa fazer o seu trabalho, é obrigatório um 
    * {@link EntityManager EntityManager} para acessar o banco.
    */
  @PostConstruct
  public void postConstruct(){
      if( em == null ){
          String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
          Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
          throw new MsgException(JsonResponse.ERROR_DESCONHECIDO,null,msg);
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
    checkNull(json);
    Map<String, Object> resposta = new HashMap<>( json.size() + 2, 1 );
    for(JsonNode node : json){
      Map resp = entService.tipo( cache.get( node.asText() ) );
      resposta.put( node.asText(), resp );
    }
    return new JsonResponse(true, resposta, "Tipo muitos");
  }
  
  @POST
  @Path("/buscar")
  @Transactional
  public JsonResponse buscar(JsonNode json) {
    checkNull(json);
    List<PersistenciaUtils.BuscaInfo> buscas = parseBusca(json);
    Map<String, Object> resposta = new HashMap<>( json.size() + 2, 1 );
    for( PersistenciaUtils.BuscaInfo info : buscas ){
      List lista = entService.buscar(info);
      resposta.put(info.entidade, lista);
    }
    return new JsonResponse(false, resposta, "Buscar muitos");
  }

  @POST
  @Path("/")
  @Transactional
  public JsonResponse criar(JsonNode json) {
    checkNull(json);
    for(JsonNode node : json){
      if( !node.has("entidade") && (!node.has("data") || !node.get("data").isArray()) ) continue;
      String entidade = node.get("entidade").asText();
      ///////////////////////////////////////////////////////////////////////////
    }
    return new JsonResponse(false, null, "Criar muitos");
  }

  @PUT
  @Path("/")
  @Transactional
  public JsonResponse editar(JsonNode json) {
    checkNull(json);
    List<PersistenciaUtils.BuscaInfo> buscas = parseBusca(json);
    Map<String, Object> resposta = new HashMap<>( json.size() + 2, 1 );
    for( PersistenciaUtils.BuscaInfo info : buscas ){
      int qtd = entService.editar(info, (JsonNode)info.data );
      resposta.put(info.entidade, qtd);
    }
    return new JsonResponse(false, resposta, "Editar muitos");
  }

  @DELETE
  @Path("/")
  @Transactional
  public JsonResponse deletar(JsonNode json) {
    checkNull(json);
    List<PersistenciaUtils.BuscaInfo> buscas = parseBusca(json);
    Map<String, Object> resposta = new HashMap<>( json.size() + 2, 1 );
    for( PersistenciaUtils.BuscaInfo info : buscas ){
      int qtd = entService.deletar( info );
      resposta.put(info.entidade, qtd);
    }
    return new JsonResponse(false, resposta, "Deletar muitos");
  }
  
  
  //===========================================================================
  private void checkNull(JsonNode json){
    if( json == null || !json.isArray() ) throw new MsgException(JsonResponse.ERROR_EXCECAO,null,
            "Para usar estes serviços é necessário enviar uma mensagem JSON para o servidor");
  }
  private List<PersistenciaUtils.BuscaInfo> parseBusca( JsonNode json ){
    List<PersistenciaUtils.BuscaInfo> buscas = new ArrayList<>();
    for( JsonNode node : json ){
      PersistenciaUtils.BuscaInfo busca = new PersistenciaUtils.BuscaInfo();
      busca.entidade = node.get("entidade").asText();
      busca.classe = cache.get(busca.entidade);
      if( node.has("size") ) busca.size = node.get("size").intValue();
      if( node.has("page") ) busca.page = node.get("page").intValue();
      if( node.has("data") ) busca.data = node.get("data");
      if( node.has("join") && node.get("join").isArray() ){
        List<String> arr = new ArrayList<>();
        for( JsonNode nodeStr : node.get("join") ) arr.add( nodeStr.asText() );
        busca.join = arr.toArray(new String[0]);
      }
      if( node.has("order") && node.get("order").isArray() ){
        List<String> arr = new ArrayList<>();
        for( JsonNode nodeStr : node.get("order") ) arr.add( nodeStr.asText() );
        busca.order = arr.toArray(new String[0]);
      }
      if( node.has("query") && node.get("query").isTextual() ){
        busca.query = PersistenciaUtils.parseQueryString( node.get("query").asText() );
      }
      
      if( busca.join == null ) busca.join = new String[0];
      if( busca.order == null ) busca.order = new String[0];
      if( busca.query == null ) busca.query = new String[0][];
      
      buscas.add(busca);
    }
    return buscas;
  }
  
  
}
