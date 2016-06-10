package br.eng.rcc.framework.filtros;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExceptionFiltro implements Filter {
  
  private static Logger log = LogManager.getLogger(ExceptionFiltro.class);
  
  private ObjectMapper mapper;

  //======================================================
  @Override
  public void init(FilterConfig fc) throws ServletException {
    mapper = new JacksonObjectMapperContextResolver().getContext(null);
  }

  @Override
  public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc)
          throws IOException, ServletException {
    try {
      fc.doFilter(sr, sr1);
    } catch (MsgException ex) {
      JsonResponse res = new JsonResponse(false, ex.getCodigo(), ex.getData(), ex.getMessage());
      mapper.writeValue(sr1.getOutputStream(), res);
    } catch (Exception ex) {
      log.error("Exceção inexperada: {}", ex.getMessage());
      JsonResponse res = new JsonResponse(false, JsonResponse.ERROR_DESCONHECIDO, null, ex.getMessage());
      mapper.writeValue(sr1.getOutputStream(), res);
    }
  }

  @Override
  public void destroy() {

  }

}
