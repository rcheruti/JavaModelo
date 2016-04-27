
package br.eng.rcc.framework.export.excel;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.utils.ClassCache;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet(value="/exportar/*")
public class ExcelServlet extends HttpServlet{
  
  @Inject
  private ClassCache cache;
  @Inject
  private UtilsDownloadExcel utilsExcel;
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
    
    String val = req.getParameter("json");
    if( val == null ){
      throw new MsgException("Envie um JSON no corpo da mensagem, no parâmetro de formulário 'json'");
    }
    
    
    JsonNode json = mapper.readTree( val );
    
    List<BuscaInfo> buscas = PersistenciaUtils.parseBusca(json, cache);
    try{
      BuscaInfo info = buscas.get(0);
      XSSFWorkbook book = utilsExcel.exportarEntidade( buscas.get(0) );
      
      String nome = info.data.path(0).path("nome").asText();
      
      resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      resp.addHeader("Content-Disposition: ", "attachment; filename=\""+ nome +".xlsx\"");
      
      book.write( resp.getOutputStream() );
      resp.flushBuffer();
      
    }catch(Exception ex){
      System.out.printf("Alguma coisa deu errado! \n");
      throw new RuntimeException(ex);
    }
    
  }
  
}
