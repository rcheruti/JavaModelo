package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

public class PersistenciaUtils {
  
  private static final Pattern queryStringPattern = Pattern
       .compile("([\\w.]++)\\s*+(=|!=|<|>|<=|>=|(?:not)?like|is(?:not)?null)\\s*+((['\"]).*?\\4|[\\w\\.]++)\\s*+([&\\|]?)", Pattern.CASE_INSENSITIVE);
  private static final Pattern valorPattern = Pattern
       .compile("^(['\"]).*\\1$", Pattern.CASE_INSENSITIVE);
  
  private static ObjectMapper mapper;
  
  
  
  public static void resolverLazy(ClassCache cache, Object[] lista, 
          boolean anular, String... params){
    if( lista == null || lista.length == 0 ) return;
    Class klass = null;
    for( Object ooo : lista ){
      if( ooo != null ){
        klass = ooo.getClass();
        break;
      }
    }
    if( klass == null ) return;
    Map<String, ClasseAtributoUtil> map = cache.getInfo( klass.getSimpleName() );
    List<ClasseAtributoUtil> fieldsEncontrados = new ArrayList<>();
    List<ClasseAtributoUtil> fieldsToNull = new ArrayList<>();
      
    try{
      String[] nParams = getArrayLevel(-1, null, params );
      for( ClasseAtributoUtil bu : map.values() ){
        if( bu.isAssociacao() || bu.isEmbutido() ){
          if( constainsInArray(nParams, bu.getNome()) ){
            fieldsEncontrados.add(bu);
          }else{
            if( anular ) fieldsToNull.add(bu);
          }
        }
      }
      
      for( ClasseAtributoUtil bu : fieldsToNull ){
        for( Object obj : lista ){
          if( obj == null ) continue;
          bu.set(obj, null);
        }
      }
      //String[] novoParams = copyWithoutNulls(params);
      for( ClasseAtributoUtil bu : fieldsEncontrados ){
        String[] novoParams = getArrayLevel(1, bu.getNome(), params );
        for( Object obj : lista ){
          if( obj == null ) continue;
          Object novoObj = bu.get(obj);
          if( novoObj == null ) continue;
          Object[] novoLista;
          if( novoObj instanceof Collection ) novoLista = ((Collection)novoObj).toArray();
          else novoLista = new Object[]{ novoObj };
          resolverLazy(cache, novoLista, anular, novoParams);
        }
      }
    }catch(NullPointerException ex){
      throw new RuntimeException("Algum dos métodos Setter JavaBeans disparou NullPointerException!", ex);
    }catch(IllegalAccessException | InvocationTargetException ex){
      throw new RuntimeException("Essa JVM não tem permissão para executar atividades de Reflexão ou Introspecção!", ex);
    }
  }
  
  
  public static boolean constainsInArray(Object[] arr, Object obj) {
    if (arr == null || obj == null) {
      return false;
    }
    int hash = obj.hashCode();
    for (int i = 0; i < arr.length; i ++) {
      Object x = arr[i];
      if( x == null )continue;
      if (hash == x.hashCode() && obj.equals(x)) {
        return true;
      }
    }
    return false;
  }
  private static String[] getArrayLevel( int level, String prefix, String... arr ){
    if( arr == null || arr.length == 0 ) return null;
    Set<String> lista = new HashSet<>();
    if( level < 0 ){
      for( String s : arr ){
        if( s != null ){
          lista.add( s.split("\\.")[0] );
        }
      }
      return lista.toArray(new String[0]);
    }
    for(  String s : arr ){
      if( s != null && ( prefix == null || s.startsWith(prefix) ) ){
        String[] split = s.split("\\.");
        if( split.length < level ) continue;
        String sf = "";
        for(int i = level; i < split.length; i++) sf += String.format("%s.", split[i]);
        if( sf.length() < 3 ) continue;
        lista.add( sf.substring(0, sf.length()-1) );
      }
    }
    return lista.toArray(new String[0]);
  }
  
  
  public static List<BuscaInfo> parseBusca(JsonNode json, ClassCache cache){
    List<BuscaInfo> buscas = new ArrayList<>();
    Iterable<JsonNode> its;
    if( json.isArray() ) its = json;
    else{
      List<JsonNode> itsArr = new ArrayList<>(1);
      itsArr.add(json);
      its = itsArr;
    }
    for( JsonNode node : its ){
      BuscaInfo busca = new BuscaInfo();
      busca.entidade = node.get("entidade").asText();
      busca.classe = cache.get(busca.entidade);
      if( !node.path("data").isArray() ){
        if( mapper == null ){
          mapper = new JacksonObjectMapperContextResolver().getContext(null);
        }
        busca.data = mapper.createArrayNode();
        busca.data.add( node.path("data") );
      }else{
        busca.data = (ArrayNode)node.get("data");
      }
      
      if( node.has("size") ) busca.size = node.get("size").intValue();
      if( node.has("page") ) busca.page = node.get("page").intValue();
      if( node.has("id") ) busca.id = node.get("id").booleanValue();
      if( node.has("acao") ) busca.acao = (byte)node.get("acao").intValue();
      if( node.has("join") && node.get("join").isArray() ){
        busca.join = new ArrayList<>();
        for( JsonNode nodeStr : node.get("join") ) busca.join.add( nodeStr.asText() );
      }
      if( node.has("order") && node.get("order").isArray() ){
        busca.order = new ArrayList<>();
        for( JsonNode nodeStr : node.get("order") ) busca.order.add( nodeStr.asText() );
      }
      
      if( node.has("where") ) busca.where = PersistenciaUtils.parseQueryString( node.get("where").asText() );
      
      if( busca.join == null ) busca.join = new ArrayList<>();
      if( busca.order == null ) busca.order = new ArrayList<>();
      if( busca.where == null ) busca.where = new ArrayList<>();
      
      buscas.add(busca);
    }
    return buscas;
  }
  
  
  /**
   * Este método é usado internamente para criar os itens de filtragem das
   * buscas.
   *
   * @param uriQuery
   * @return
   */
  public static List<String[]> parseQueryString(String uriQuery) {
    // Interpretando "Query String" (parâmetros de busca no banco [WHERE])
    List<String[]> querysPs = new ArrayList<>(  );
    if (uriQuery != null) {
      Matcher m = queryStringPattern.matcher(uriQuery);
      while (m.find()) {
        String attr = m.group(1);
        String comp = m.group(2);
        String valor = m.group(3);
        String opComp = m.group(5);
        if (attr == null || comp == null || valor == null) {
          continue;
        }

        boolean isValor = valorPattern.matcher(valor).find();
        if (isValor) {
          valor = valor.substring(1, valor.length() - 1);
        }

        String[] params = {attr, comp, valor, opComp, isValor ? "" : null};
        querysPs.add( params );
      }
    }
    return querysPs;
  }
  
  
  public static List<String> getIds(EntityManager em, Class<?> klass){
    Metamodel meta = em.getMetamodel();
    EntityType entity = meta.entity( klass );
    List<String> nomes = new ArrayList<>();
    
    try{
      Set<SingularAttribute> attrs = entity.getIdClassAttributes();
      if( attrs!= null ){
        for( SingularAttribute attr : attrs ){
          nomes.add( attr.getName() );
        }
      }
      return nomes; 
    }catch(IllegalArgumentException ex){
      Set<SingularAttribute> ids = new HashSet<>(4);
      for( Attribute attr : (Set<Attribute>)entity.getAttributes() ){
        Field field = (Field)attr.getJavaMember();
        Id id = field.getAnnotation( Id.class );
        if( id != null ){
          ids.add( (SingularAttribute)attr );
          nomes.add( attr.getName() );
        }else{
          EmbeddedId embId = field.getAnnotation( EmbeddedId.class );
          if( embId != null ){
            //ids.add( (SingularAttribute)attr );
            String nomeEmb = attr.getName();
            Class c = attr.getJavaType();
            for( Field fC : c.getDeclaredFields() ){
              nomes.add( String.format("%s.%s", nomeEmb, fC.getName() ) );
            }
          }
        }
      }
      return nomes;
    }
  }
  
  
}
