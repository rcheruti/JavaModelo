package br.eng.rcc.framework.listeners;

import br.eng.rcc.framework.filtros.CORSFiltro;
import br.eng.rcc.framework.filtros.ExceptionFiltro;
import br.eng.rcc.framework.filtros.RewriteFiltro;
import br.eng.rcc.framework.seguranca.filtros.SegurancaFiltro;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Essa classe será usada para configurar os filtros que deverão estar
 * disponípiveis nos servlets desse sistema.
 *
 * @author rcheruti
 */
@WebListener
public class FiltroRegistrador implements ServletContextListener {
  
  private static Logger log = LogManager.getLogger(FiltroRegistrador.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    log.info("Registrando os filtros (servlet) do sistema.");
    
    ServletContext ctx = sce.getServletContext();
    
    log.debug("---  Filtro: {}", CORSFiltro.class.getCanonicalName());
    ctx.addFilter(CORSFiltro.class.getName(), CORSFiltro.class)
            .addMappingForUrlPatterns(null, true, "/*");
    
    log.debug("---  Filtro: {}", ExceptionFiltro.class.getCanonicalName());
    ctx.addFilter(ExceptionFiltro.class.getName(), ExceptionFiltro.class)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST,
                    DispatcherType.FORWARD,
                    DispatcherType.INCLUDE), true, "/*");
    
    log.debug("---  Filtro: {}", RewriteFiltro.class.getCanonicalName());
    ctx.addFilter(RewriteFiltro.class.getName(), RewriteFiltro.class)
            .addMappingForUrlPatterns(null, true, "/*");
    
    log.debug("---  Filtro: {}", SegurancaFiltro.class.getCanonicalName());
    ctx.addFilter(SegurancaFiltro.class.getName(), SegurancaFiltro.class)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
    
    log.info("Registro de filtros (servlet) finalizado.");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }

}
