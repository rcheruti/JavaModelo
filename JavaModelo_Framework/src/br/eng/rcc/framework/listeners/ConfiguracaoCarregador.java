
package br.eng.rcc.framework.listeners;

import br.eng.rcc.framework.Configuracoes;
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
public class ConfiguracaoCarregador implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext(); 
        
        String s = null;
        
        s = ctx.getInitParameter("Configuracoes.loginCookieName");
        if( s != null ) Configuracoes.loginCookieName = s;
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
}
