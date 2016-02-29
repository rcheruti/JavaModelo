package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.jaxrs.persistencia.ClassCache;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
       .compile("([\\w.]++)\\s*+(=|!=|<|>|<=|>=|(?>not)?like)\\s*+((['\"]).*?\\4|[\\w\\.]++)\\s*+([&\\|]?)", Pattern.CASE_INSENSITIVE);
  private static final Pattern valorPattern = Pattern
       .compile("^(['\"]).*\\1$", Pattern.CASE_INSENSITIVE);
  private static final Pattern matrixPattern = Pattern
       .compile(";\\s*([^=\\s]+)=([^;]+)");
  private static final Pattern entidadePattern = Pattern
       .compile("/persistencia/(?:um/(?:id/)?)?([\\w\\d]+)", Pattern.CASE_INSENSITIVE);
  
  
  
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
    Map<String, ClassCache.BeanUtil> map = cache.getInfo( klass.getSimpleName() );
    List<ClassCache.BeanUtil> fieldsEncontrados = new ArrayList<>();
    List<ClassCache.BeanUtil> fieldsToNull = new ArrayList<>();
      
    try{
      String[] nParams = getArrayLevel(-1, null, params );
      for( ClassCache.BeanUtil bu : map.values() ){
        if( bu.isAssociacao() ){
          if( constainsInArray(nParams, bu.getNome()) ){
            fieldsEncontrados.add(bu);
          }else{
            if( anular ) fieldsToNull.add(bu);
          }
        }
      }
      
      for( ClassCache.BeanUtil bu : fieldsToNull ){
        for( Object obj : lista ){
          if( obj == null ) continue;
          bu.set(obj, null);
        }
      }
      //String[] novoParams = copyWithoutNulls(params);
      for( ClassCache.BeanUtil bu : fieldsEncontrados ){
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
  private static boolean nullifyInArray(Object[] arr, Object obj) {
    if (arr == null || obj == null) {
      return false;
    }
    int hash = obj.hashCode();
    for (int i = 0; i < arr.length; i ++) {
      Object x = arr[i];
      if( x == null )continue;
      if (hash == x.hashCode() && obj.equals(x)) {
        arr[i] = null;
        return true;
      }
    }
    return false;
  }
  private static <T> T[] copyWithoutNulls( T[] arr ){
    if( arr == null ) return null;
    List<T> newArr = new ArrayList<>( arr.length /2 );
    for(int i = 0; i < arr.length; i++){
      if( arr[i] != null ) newArr.add( arr[i] );
    }
    return newArr.toArray(arr);
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
  
  
  public static BuscaInfo parseBusca(String uriQuery){
    return parseBusca(uriQuery, null);
  }
  public static BuscaInfo parseBusca(String uriQuery, ClassCache cache){
    if( uriQuery == null ) return null;
    String[] uriSplit = uriQuery.split("\\?");
    String[][] query = uriSplit.length > 1? parseQueryString( uriSplit[1] ) : new String[0][0];
    String[][] matrix = uriSplit.length > 0? parseMatrixString( uriSplit[0] ) : new String[0][0];
    
    BuscaInfo bi = new BuscaInfo();
    bi.query = query;
    try{
      if( matrix[0] != null && matrix[0][0] != null ) bi.size = Integer.parseInt(matrix[0][0]);
      if( matrix[1] != null && matrix[1][0] != null ) bi.page = Integer.parseInt(matrix[1][0]);
    }catch( NumberFormatException ex ){
      throw new MsgException(JsonResponse.ERROR_EXCECAO, "Os parâmetros 'size' e 'page' devem ser números inteiros!", ex);
    }
    bi.join = matrix[2] != null ? matrix[2] : new String[0];
    bi.order = matrix[3] != null ? matrix[3] : new String[0];
    
    Matcher matcher = entidadePattern.matcher(uriQuery);
    if( matcher.find() ){
      String entidade = matcher.group(1);
      Class klass = null;
      if( cache != null ){
        klass = cache.get(entidade);
      }
      bi.entidade = entidade;
      bi.classe = klass;
    }
    
    return bi;
  }
  
  /**
   * Este método é usado internamente para criar os itens de filtragem das
   * buscas.
   *
   * @param uriQuery
   * @return
   */
  public static String[][] parseQueryString(String uriQuery) {
    // Interpretando "Query String" (parâmetros de busca no banco [WHERE])
    //List<String[]> querysPs = new ArrayList<>();
    int qI = 0, qLimit = 30;
    String[][] querysPs = new String[qLimit][];
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
        //querysPs.add(params);
        querysPs[qI++] = params;
      }
    }
    String[][] resQueryPs = new String[qI][];
    System.arraycopy(querysPs, 0, resQueryPs, 0, qI);
    return resQueryPs;
  }
  
  public static String[][] parseMatrixString(String uriQuery){
    String[][] resp = new String[4][];
    if( uriQuery == null ) return resp;
    
    Matcher matcher = matrixPattern.matcher(uriQuery);
    String[] split;
    while( matcher.find() ){
      String nome = matcher.group(1).trim().toLowerCase();
      String valor = matcher.group(2).trim();
      switch(nome){
        case "size":
          resp[0] = new String[]{ valor };
          break;
        case "page":
          resp[1] = new String[]{ valor };
          break;
        case "join":
          split = valor.split(",");
          for( int i = 0; i < split.length; i++ ){
            split[i] = split[i].trim();
          }
          resp[2] = split;
          break;
        case "order":
          split = valor.split(",");
          for( int i = 0; i < split.length; i++ ){
            split[i] = split[i].trim();
          }
          resp[3] = split;
          break;
      }
    }
    
    return resp;
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
  
  
  //=============================================================================
  
  public static class BuscaInfo {

    private int page = Configuracoes.pageEntidadeDefault;
    private int size = Configuracoes.sizeEntidadeDefault;
    private String[] join;
    private String[] order;
    private String[][] query;
    private String entidade;
    private Class<?> classe;
    
    
    public int getPage(){ return page; }
    public int getSize(){ return size; }
    public String[] getJoin(){ return join; }
    public String[] getOrder(){ return order; }
    public String[][] getQuery(){ return query; }
    public String getEntidade(){ return entidade; }
    public Class<?> getClasse(){ return classe; }

  }

  
  
}
