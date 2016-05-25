
package br.eng.rcc.framework.listeners;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import br.eng.rcc.framework.utils.ClassCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * !!! Atenção !!! As configs que deverão ser carregadas devem inserir as relações 
 * de FK do banco. Como será no JSON?
 * 
 * @author rcheruti
 */

@Deprecated
//@WebListener
public class DBLoader implements ServletContextListener{
  
  
  @Inject
  private UsuarioServico uServ;
  @Inject 
  private EntityManager em;
  @Inject
  private ClassCache cache;
  
  
  public void loadDB(){
    System.out.printf("---  Executando 'DBLoader' para preencher o banco de dados. \n");
    
    if( Configuracoes.getInstance().carregarDB() != null 
            && Configuracoes.getInstance().carregarDB().size() > 0 ){
      URL url = DBLoader.class.getClassLoader().getResource("META-INF/dbloader.json");
      if( url == null ) url = DBLoader.class.getClassLoader().getResource("dbloader.json");
      
      if( url != null ){
        try{
          ObjectMapper mapper = new JacksonObjectMapperContextResolver().getContext(null);
          JsonNode json = mapper.readTree(url);
          
          for( String key : Configuracoes.getInstance().carregarDB() ){
            JsonNode entJson = json.get(key);
            if( entJson == null ) continue;
            
            Iterator<String> entKeys = entJson.fieldNames();
            while( entKeys.hasNext() ){
              String entKey = entKeys.next();
              
              Class<?> klass = cache.get( entKey, em, true );
              if( klass == null ){
                System.out.printf("+++  Atenção!! a chave '%s' não é o nome de uma entidade! \n", entKey);
                continue;
              }
              
              JsonNode node = entJson.get(entKey);
              List<Object> objs;
              if( node.isArray() ){
                objs = new ArrayList<>( node.size() + 1 );
                for( JsonNode n : node ){
                  objs.add( mapper.treeToValue( n, klass ) );
                }
              }else{
                objs = Arrays.asList( mapper.treeToValue( node, klass ) );
              }
              
              for(Object ooo : objs) em.persist(ooo);
              
            }
          }
          em.flush();
          
        }catch(IOException ex){
          System.out.printf("+++  Não foi possível carregar o arquivo, uma excessão foi disparada! \n");
          ex.printStackTrace();
        }
        
        em.clear();
      }
    }else{
      System.out.printf("---  Nenhum nome de chave configurado para o DBLoader, o arquivo de config não será carregado. \n");
    }
    
    System.out.printf("---  Execução de 'DBLoader' finalizada. \n");
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    
  }
  
}
