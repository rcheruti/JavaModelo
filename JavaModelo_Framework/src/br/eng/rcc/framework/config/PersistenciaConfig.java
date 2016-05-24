
package br.eng.rcc.framework.config;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.seguranca.config.SegurancaNode;
import br.eng.rcc.framework.seguranca.config.SegurancaRootNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Essa classe faz a leitura dos arquivos de configuraçao de segurança, que 
 * pode ser escrito em <strong>JSON</strong> ou <i>XML (este ainda está pendente!!!)</i>
 * 
 * @author Rafael
 */
public class PersistenciaConfig {
    
  private static SegurancaRootNode segurancas;
  
  public static SegurancaRootNode getSegurancas(){ return segurancas; }
  
  
  public static void init() throws IOException{
    System.out.printf("---  Carregando configurações do arquivo 'persistencia.json'. \n");
    
    URL url = PersistenciaConfig.class.getClassLoader().getResource("META-INF/persistencia.json");
    if( url == null ) url = PersistenciaConfig.class.getClassLoader().getResource("persistencia.json");

    if( url != null ){
      ObjectMapper mapper = new JacksonObjectMapperContextResolver().getContext(null);
      JsonNode json = mapper.readValue(url, JsonNode.class);
      
      Configuracoes.load( mapper.convertValue(json, Map.class) );
      
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

    System.out.printf("---  Configurações de 'persistencia.json' finalizado. \n");
  }
  
  
  
  private static Map<String,Object> loadConfig(JsonNode json){
    
    
    Map<String,Object> mapConfigs = new HashMap<>(16);
    Iterator<String> itN = json.fieldNames();
    while( itN.hasNext() ){
      String s = itN.next();
      JsonNode node = json.get(s);
      if( node.isArray() ){
        
      }else if( node.isObject() ){
        
      }else{
        mapConfigs.put(s, node.asText() );
      }
      
      /*
      }else if( node.isDouble() || node.isFloat() ){
        mapConfigs.put(s, node.asDouble() );
      }else if( node.isInt() ){
        mapConfigs.put(s, node.asInt() );
      }else if( node.isLong() ){
        mapConfigs.put(s, node.asLong() );
      */
      
    }
    

      // ===  Carregando configs do Hibernate:
    if( json.path("hibernate").isObject() ){
      JsonNode jsonHibernate = json.path("hibernate");
      Iterator<String> itNames = jsonHibernate.fieldNames();
      while( itNames.hasNext() ){
        String s = itNames.next();
        Configuracoes.hibernate.put(s, jsonHibernate.get(s).asText());
      }
    }
    
    return mapConfigs;
  }
  
}
