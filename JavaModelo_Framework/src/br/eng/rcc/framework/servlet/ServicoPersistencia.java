
package br.eng.rcc.framework.servlet;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.jaxrs.persistencia.ClassCache;
import br.eng.rcc.framework.jaxrs.persistencia.EntidadesService;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
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
    if( !json.isArray() ){
      throw new MsgException("O formato do JSON é: [ { /*.Busca1.*/ }, { /*.Busca2.*/, ... } ]");
    }
    
    List<BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    List<Object> resposta = new ArrayList<>( buscas.size() );
    Object ooo = null;
    for( BuscaInfo busca : buscas ){
      switch( busca.acao ){
        case BuscaInfo.ACAO_TIPO:
          ooo = entService.tipo( busca.classe );
          break;
        case BuscaInfo.ACAO_BUSCAR:
          ooo = entService.buscar( busca );
          break;
        case BuscaInfo.ACAO_CRIAR:
          ooo = entService.criar( busca );
          break;
        case BuscaInfo.ACAO_EDITAR:
          ooo = entService.editar( busca );
          break;
        case BuscaInfo.ACAO_DELETAR:
          ooo = entService.deletar( busca );
          break;
        case BuscaInfo.ACAO_ADICIONAR:
          throw new MsgException("Ainda não existe impl. para ADICIONAR");
          //break;
        case BuscaInfo.ACAO_REMOVER:
          throw new MsgException("Ainda não existe impl. para REMOVER");
          //break;
      }
      resposta.add(ooo);
    }
    
    mapper.writeValue(resp.getWriter(), resposta);
    
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
