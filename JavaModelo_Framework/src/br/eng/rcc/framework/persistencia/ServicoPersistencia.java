
package br.eng.rcc.framework.persistencia;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.persistencia.ClassCache;
import br.eng.rcc.framework.persistencia.EntidadesService;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value="/persistencia")
public class ServicoPersistencia extends HttpServlet{
  
  @Inject
  private ClassCache cache;
  @Inject
  private EntityManager em;
  @Inject
  private EntidadesService entService;
  @Inject
  private JacksonObjectMapperContextResolver resolver;
  
  private ObjectMapper mapper;

  @Override
  public void init() throws ServletException {
    mapper = resolver.getContext(null);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    
    JsonNode json = mapper.readTree( req.getInputStream() );
    if( json == null ){
      throw new MsgException("Envie um JSON no corpo da mensagem");
    }
    
    List<BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    List<Object> resposta = new ArrayList<>( buscas.size() );
    Object ooo = null;
    for( BuscaInfo busca : buscas ){
      try{
        switch( busca.acao ){
          case BuscaInfo.ACAO_TIPO:
            ooo = entService.tipo( busca.classe );
            break;
          case BuscaInfo.ACAO_BUSCAR:
            if( busca.id ){
              List lista = new ArrayList<>();
              List<String> ids = PersistenciaUtils.getIds(em, busca.classe);
              if (ids == null || ids.isEmpty()) {
                throw new MsgException(String
                  .format("Não encontramos os campos de Id dessa classe: '%s'", busca.entidade));
              }

              Iterable<JsonNode> it;
              if( busca.data.isArray() ) it = busca.data;
              else{
                List x = new ArrayList(1);
                x.add( busca.data );
                it = x;
              }
              for( JsonNode node : it ){
                if( node == null || !node.isObject() ) continue;
                busca.query = new String[ ids.size() ][];
                int i = 0;
                for( String idAttr : ids ){
                  String[] idS = idAttr.split("\\.");
                  JsonNode prop = node;
                  for( String s : idS ) prop = prop.get(s);
                  if( prop == null ) continue;
                  busca.query[i++] = new String[]{ idAttr, prop.isNull()?"isnull":"=", prop.asText(), "&" };
                }

                List listaBusca = entService.buscar(busca);
                for( Object x : listaBusca ) lista.add(x);
              }
              ooo = lista;
            }else{
              ooo = entService.buscar( busca );
            }
            break;
          case BuscaInfo.ACAO_CRIAR:
            if( busca.id ){
              throw new MsgException("Não é permitido criar entidades a partir do ID");
            }
            ooo = entService.criar( busca );
            break;
          case BuscaInfo.ACAO_EDITAR:
            if( busca.id ){
              int res = 0;
              List<String> ids = PersistenciaUtils.getIds(em, busca.classe);
              if (ids == null || ids.isEmpty()) {
                throw new MsgException(String
                  .format("Não encontramos os campos de Id dessa classe: '%s'", busca.entidade));
              }
              
              Iterable<JsonNode> it;
              if( busca.data.isArray() ) it = busca.data;
              else{
                List x = new ArrayList(1);
                x.add( busca.data );
                it = x;
              }
              for( JsonNode node : it ){
                if( node == null || !node.isObject() ) continue;
                busca.query = new String[ ids.size() ][];
                int i = 0;
                for( String idAttr : ids ){
                  String[] idS = idAttr.split("\\.");
                  JsonNode prop = node;
                  for( String s : idS ) prop = prop.get(s);
                  if( prop == null ) continue;
                  busca.query[i++] = new String[]{ idAttr, prop.isNull()?"isnull":"=", prop.asText(), "&" };
                }

                res += entService.editar(busca);
              }
              ooo = res;
            }else{
              ooo = entService.editar( busca );
            }
            break;
          case BuscaInfo.ACAO_DELETAR:
            if( busca.id ){
              int res = 0;
              List<String> ids = PersistenciaUtils.getIds(em, busca.classe);
              if (ids == null || ids.isEmpty()) {
                throw new MsgException(String
                  .format("Não encontramos os campos de Id dessa classe: '%s'", busca.entidade));
              }

              Iterable<JsonNode> it;
              if( busca.data.isArray() ) it = busca.data;
              else{
                List x = new ArrayList(1);
                x.add( busca.data );
                it = x;
              }
              for( JsonNode node : it ){
                if( node == null || !node.isObject() ) continue;
                busca.query = new String[ ids.size() ][];
                int i = 0;
                for( String idAttr : ids ){
                  String[] idS = idAttr.split("\\.");
                  JsonNode prop = node;
                  for( String s : idS ) prop = prop.get(s);
                  if( prop == null ) continue;
                  busca.query[i++] = new String[]{ idAttr, prop.isNull()?"isnull":"=", prop.asText(), "&" };
                }

                res += entService.deletar(busca);
              }
              ooo = res;
            }else{
              ooo = entService.deletar( busca );
            }
            break;
          case BuscaInfo.ACAO_ADICIONAR:
            throw new MsgException("Ainda não existe impl. para ADICIONAR");
            //break;
          case BuscaInfo.ACAO_REMOVER:
            throw new MsgException("Ainda não existe impl. para REMOVER");
            //break;
        }
        resposta.add(ooo);
      }catch(MsgException ex){
        resposta.add( new JsonResponse(false, ex.getCodigo(),
                ex.getData(), ex.getMensagem()) );
      }
    }
    
    
    mapper.writeValue(resp.getWriter(), new JsonResponse(true, resposta, "") );
    
  }
  
  
  
  
  
  
  
  //===========================================================================
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    super.doPost(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    super.doPost(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    super.doPost(req, resp);
  }
  //===========================================================================
  
  
}
