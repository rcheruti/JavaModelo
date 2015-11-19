
package br.eng.rcc.framework.listeners;

import br.eng.rcc.framework.filtros.ExceptionFiltro;
import br.eng.rcc.framework.filtros.RewriteFiltro;
import br.eng.rcc.seguranca.filtros.SegurancaFiltro;
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
        ctx.addFilter(ExceptionFiltro.class.getName(), ExceptionFiltro.class);
        ctx.addFilter(RewriteFiltro.class.getName(), RewriteFiltro.class);
        ctx.addFilter(SegurancaFiltro.class.getName(), SegurancaFiltro.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
}
