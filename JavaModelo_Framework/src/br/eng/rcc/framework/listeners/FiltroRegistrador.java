
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

/**
 * Essa classe será usada para configurar os filtros que deverão estar disponípiveis
 * nos servlets desse sistema.
 * @author rcheruti
 */
@WebListener
public class FiltroRegistrador implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext(); 
        ctx.addFilter(CORSFiltro.class.getName(), CORSFiltro.class)
          .addMappingForUrlPatterns(null, true, "/*");
        ctx.addFilter(ExceptionFiltro.class.getName(), ExceptionFiltro.class)
          .addMappingForUrlPatterns(EnumSet.of( DispatcherType.REQUEST, 
                                                DispatcherType.FORWARD, 
                                                DispatcherType.INCLUDE ), true, "/*");
        ctx.addFilter(SegurancaFiltro.class.getName(), SegurancaFiltro.class)
          .addMappingForUrlPatterns(EnumSet.of( DispatcherType.REQUEST ) , true, "/*");
        ctx.addFilter(RewriteFiltro.class.getName(), RewriteFiltro.class)
          .addMappingForUrlPatterns(null, true, "/*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
}
