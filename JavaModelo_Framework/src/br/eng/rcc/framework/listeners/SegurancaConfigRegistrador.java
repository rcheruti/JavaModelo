
package br.eng.rcc.framework.listeners;

import br.eng.rcc.framework.config.Configuracoes;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Essa classe inicia o processo de configuração das seguranças.
 * 
 * As seguranças são lidas de um arquivo de configuração (assim como também são
 * declaradas no código, nos métodos ou nas entidades).
 * 
 * Essa classe inicia a leitura dos arquivos de configuração das seguranças
 * principalmente para permitir a segunraça de endereços (URLs).
 * 
 */
@WebListener
public class SegurancaConfigRegistrador implements ServletContextListener{

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    Configuracoes.carregar();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    
  }
    
    
    
}
