
package br.eng.rcc.framework.config;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.seguranca.config.SegurancaNode;
import br.eng.rcc.framework.seguranca.config.SegurancaRootNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Essa classe faz a leitura dos arquivos de configuraçao de segurança, que 
 * pode ser escrito em <strong>JSON</strong> ou <i>XML (este ainda está pendente!!!)</i>
 * 
 */
public class PersistenciaConfig {
  
  private static Logger log = LogManager.getLogger();
    
  private static SegurancaRootNode segurancas;
  
  public static SegurancaRootNode getSegurancas(){ return segurancas; }
  
  
  public static void init() throws IOException{
    log.info("Carregando configurações do arquivo 'persistencia.json'.");
    
    URL url = PersistenciaConfig.class.getClassLoader().getResource("META-INF/persistencia.json");
    if( url == null ) url = PersistenciaConfig.class.getClassLoader().getResource("persistencia.json");

    if( url != null ){
      ObjectMapper mapper = new JacksonObjectMapperContextResolver().getContext(null);
      JsonNode json = mapper.readValue(url, JsonNode.class);
      
      Map<String,Object> mapaCarregado = mapper.readValue(url, Map.class);
      for(String s : mapaCarregado.keySet()){
        if( Configuracoes.Key.hibernate.name().equals(s) ) continue;
        Configuracoes.getInstance().configs.put(s, mapaCarregado.get(s));
      }
      Map<String,Object> hbnMap = (Map<String,Object>)mapaCarregado.get(Configuracoes.Key.hibernate.name());
      if( hbnMap != null ){
        Configuracoes.getInstance().hibernate().putAll(hbnMap);
      }
      /*
      for(String s : Configuracoes.getInstance().configs.keySet()){
        System.out.printf("---  %s <%s>: %s \n", s, 
                Configuracoes.getInstance().configs.get(s).getClass(),
                Configuracoes.getInstance().configs.get(s));
      }
      /* */
      
        // ===  Carregando segurança:
      segurancas = new SegurancaRootNode();
      Map<String, List<SegurancaNode>> entMap = new HashMap<>();
      segurancas.setEntidades( entMap );
      
      JsonNode entidades = json.path("entidades");
      Iterator<String> nomes = entidades.fieldNames();
      while( nomes.hasNext() ){
        String nome = nomes.next();
        JsonNode nodeSeg = entidades.get(nome);
        if( !nodeSeg.isArray() || nodeSeg.size() < 1 ) continue;
        
        List<SegurancaNode> lista = entMap.get( nome );
        if( lista == null ){
          lista = new ArrayList<>(4);
          entMap.put(nome, lista);
        }
        
        SegurancaNode segNode = new SegurancaNode();
        lista.add(segNode);
        if( nodeSeg.has("select") ) segNode.setSelect( nodeSeg.get("select").asBoolean() );
        if( nodeSeg.has("insert") ) segNode.setInsert( nodeSeg.get("insert").asBoolean() );
        if( nodeSeg.has("update") ) segNode.setUpdate( nodeSeg.get("update").asBoolean() );
        if( nodeSeg.has("delete") ) segNode.setDelete( nodeSeg.get("delete").asBoolean() );
        
        if( nodeSeg.has("permissao") ) segNode.setPermissao( nodeSeg.get("permissao").asText() );
        if( nodeSeg.has("grupo") ) segNode.setGrupo(nodeSeg.get("grupo").asText() );
        
        if( nodeSeg.has("filters") ){
          List<Class<? extends SegurancaPersistenciaInterceptador>> listaF = new ArrayList<>(4);
          for( JsonNode fNome : nodeSeg.get("filters") ){
            try{
              Class klass = Class.forName( fNome.asText() );
              if( SegurancaPersistenciaInterceptador.class.isAssignableFrom(klass) ){
                listaF.add(klass);
              }
            }catch( ClassNotFoundException ex ){}
          }
          segNode.setFilters( listaF.toArray(new Class[0]) );
        }
        
      }
      
    }

    log.info("Configurações de 'persistencia.json' finalizado.");
  }
  
  
}
