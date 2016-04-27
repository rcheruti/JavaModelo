
package br.eng.rcc.framework.persistencia;

import br.eng.rcc.framework.utils.ClassCache;
import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value="/persistencia/*")
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
    
    if( req.getContentLength() < 2 ){
      throw new MsgException("Envie um JSON no corpo da mensagem");
    }
    JsonNode json = mapper.readTree( req.getInputStream() );
    
    List<BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    List<Object> resposta = entService.processar(buscas);
    
    mapper.writeValue(resp.getWriter(), new JsonResponse(true, resposta, "") );
    
  }
  
  
  
  
  
  
  
  //===========================================================================
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    this.doPost(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    
    String path = req.getPathInfo();
    if( "/context".equals(path) ){
      resp.getWriter().write( req.getContextPath() );
      resp.flushBuffer();
      return;
    }
    this.doPost(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
          throws ServletException, IOException {
    this.doPost(req, resp);
  }
  //===========================================================================
  
  
}
