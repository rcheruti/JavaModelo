
package br.eng.rcc.javamodelo.test;

import br.eng.rcc.framework.produtores.DependentEM;
import br.eng.rcc.framework.produtores.Transacional;
import br.eng.rcc.framework.seguranca.entidades.Credencial;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import java.util.List;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.transaction.Transactional;

/**
 * !!! Atenção !!! Isso precisa executar depois das configurações do framework. Como será?
 * 
 * @author rcheruti
 */

@WebListener
@Priority(Integer.MAX_VALUE)
public class DBLoaderListener implements ServletContextListener{
  
  @Inject @DependentEM
  private EntityManager em;
  @Inject
  private UsuarioServico uServ;
  
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.printf("---  Corrigindo senhas do banco de dados \n");
    Transacional.open(em);
    
    List<Credencial> lista = em.createQuery("SELECT x FROM Credencial x WHERE LENGTH(x.senha) < 60").getResultList();
    for( Credencial x : lista ){
      if( x.getSenha() == null || x.getSenha().length > 60 || x.getSenha().length < 1 ) continue;
      x.setSenha( uServ.criptografar( new String(x.getSenha()) ) );
      em.merge(x);
    }
    em.flush();
    em.clear();
    
    Transacional.close(em);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    
  }
  
  
  
}
