package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.jaxrs.persistencia.builders.WhereBuilder;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

@ApplicationScoped
public class EntidadesService {
  
  @Inject
  protected EntityManager em;
  @Inject
  protected ClassCache cache;
  @Inject
  protected SegurancaServico checker;
  @Inject
  private JacksonObjectMapperContextResolver resolver;
  
  private ObjectMapper mapper;
  
  
  //=====================================================================
  
  protected Map<Class,String> tipagem = new HashMap<>(30);
  {
    tipagem.put( boolean.class, "boolean" );
    tipagem.put( Boolean.class, "boolean" );
    tipagem.put( byte.class, "integer" );
    tipagem.put( Byte.class, "integer" );
    tipagem.put( short.class, "integer" );
    tipagem.put( Short.class, "integer" );
    tipagem.put( int.class, "integer" );
    tipagem.put( Integer.class, "integer" );
    tipagem.put( long.class, "integer" );
    tipagem.put( Long.class, "integer" );
    tipagem.put( float.class, "number" );
    tipagem.put( Float.class, "number" );
    tipagem.put( double.class, "number" );
    tipagem.put( Double.class, "number" );
    tipagem.put( char.class, "string" );
    tipagem.put( Character.class, "string" );
    tipagem.put( String.class, "string" );
    tipagem.put( Date.class, "Date" );
    tipagem.put( Calendar.class, "Date" );
    tipagem.put( Time.class, "Date" );
    tipagem.put( boolean[].class, "Blob" );
  }
  
  /**
    * Para que este objeto possa fazer o seu trabalho, é obrigatório um 
    * {@link EntityManager EntityManager} para acessar o banco.
    */
  @PostConstruct
  public void postConstruct(){
      if( em == null ){
          String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
          Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
          throw new MsgException(JsonResponse.ERROR_DESCONHECIDO,null,msg);
      }
      mapper = resolver.getContext(null);
  } 
  
  //=====================================================================
  
  public Map<String, String> tipo(Class<?> klass) {
    checker.check(klass, Seguranca.SELECT | Seguranca.INSERT
            | Seguranca.DELETE | Seguranca.UPDATE);

    Metamodel meta = this.em.getMetamodel();
    ManagedType entity = meta.managedType(klass);

    Map<String, String> map = new HashMap<>(20);
    Set<Attribute> attrs = entity.getDeclaredAttributes();
    for (Attribute attr : attrs) {
      if (attr.isCollection()) {
        PluralAttribute pAttr = (PluralAttribute) attr;
        map.put(pAttr.getName(), String.format("[%s",
                pAttr.getElementType().getJavaType().getSimpleName()));
      } else {
        String tStr = tipagem.get( attr.getJavaType() );
        if( tStr == null ) tStr = attr.getJavaType().getSimpleName();
        map.put(attr.getName(), tStr);
      }
    }

    return map;
  }

  public List<Object> buscar(BuscaInfo info) {
    checker.check( info.classe, Seguranca.SELECT );
    
    // ----  Criando a busca ao banco:
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery query = cb.createQuery();
    Root root = query.from( info.classe );
    query.select(root);

    // Cláusula JOIN FETCH da JPQL:
    if(info.join != null) for(String s : info.join ){ 
      if( s.indexOf('.') < 0 )
        root.fetch(s, JoinType.LEFT);
    }

    // Cláusula ORDER BY da JPQL:
    addOrderBy( cb, query, info.order );


    // Cláusula WHERE do banco:
    query.where( WhereBuilder.build(cb, root, info.query) );

    checker.checkPersistencia(info.classe, cb, query);
    // A busca ao banco:
    Query q = em.createQuery(query);
    q.setFirstResult( info.page * info.size );
    q.setMaxResults( info.size );
    
    // O resultado
    List<Object> res = q.getResultList();
    PersistenciaUtils.resolverLazy(cache, res.toArray(), false, info.join );
    this.em.clear();
    PersistenciaUtils.resolverLazy(cache, res.toArray(), true, info.join );

    return res;
  }

  public void criar(BuscaInfo info) throws JsonProcessingException{
    checker.check(info.classe, Seguranca.INSERT);
    List<Object> objs = new ArrayList<>();
    if( info.data.isArray() ) 
      for(JsonNode node : info.data) 
        objs.add( mapper.treeToValue(node, info.classe) );
    else objs.add( mapper.treeToValue(info.data, info.classe) );

    // precisamos colocar o objeto no lado inverso da relação para que tudo entre
    // no banco com os valores corretos
    Map<String, ClassCache.BeanUtil> map = cache.getInfo( info.entidade );
    try {
      for (ClassCache.BeanUtil util : map.values()) {
        if (util.isAssociacao()) {
          ClassCache.BeanUtil inverso = util.getInverse();
          if (inverso == null) {
            continue;
          }
          if (util.isColecao()) {
            for (Object obj : objs) {
              Collection coll = ((Collection) util.get(obj));
              if (coll == null) {
                continue;
              }
              for (Object ooo : coll) {
                inverso.set(ooo, obj);
              }
            }
          } else {
            for (Object obj : objs) {
              inverso.set(util.get(obj), obj);
            }
          }
        }
      }
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new RuntimeException("Problemas de Introspecção ou Reflexão ao criar entidades", ex);
    }
    for (Object obj : objs) {
      checker.checkPersistencia(info.classe, obj);
      em.persist(obj);
    }
  }

  public int editar(BuscaInfo info) {
    checker.check(info.classe, Seguranca.UPDATE);
    
    Map<String,Object> listaAtualizar = new HashMap<>();
    editarArvore(listaAtualizar, info.data, null);
    
    // ----  Criando a busca ao banco:
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaUpdate query = cb.createCriteriaUpdate(info.classe);
    Root root = query.from(info.classe);

    // Cláusula WHERE do banco:
    Predicate[] preds = WhereBuilder.build(cb, root, info.query);
    if (preds == null || preds.length < 1) {
      throw new MsgException(JsonResponse.ERROR_EXCECAO,null,"Os parâmetros de filtragem da QueryString não são válidos.");
    }
    query.where(preds);
    
    atualizar:
    for( String entryName : listaAtualizar.keySet() ){
      for(String[] sss : info.query) if( entryName.equals(sss[0]) ){
        continue atualizar;
      }
      String[] listaStr = entryName.split("\\.");
      if( listaStr.length < 1 ) continue;
      Path exp = root.get( listaStr[0] );
      for( int i = 1; i < listaStr.length; i++ ) exp = exp.get( listaStr[i] );
      
      Object valor = listaAtualizar.get( entryName );
        if( exp.getJavaType().equals(Date.class) ){
          valor = javax.xml.bind.DatatypeConverter.parseDateTime( (String)valor ).getTime();
        }else if( exp.getJavaType().equals(Calendar.class) ){
          valor = javax.xml.bind.DatatypeConverter.parseDateTime( (String)valor );
        }else if( exp.getJavaType().equals(Time.class) ){
          Date d = javax.xml.bind.DatatypeConverter.parseDateTime( (String)valor ).getTime();
          valor = new Time( d.getTime() );
        }
      query.set(exp, valor );
    }
    
    checker.checkPersistencia(info.classe, cb, query);

    int ups = em.createQuery(query).executeUpdate();
    return ups;
  }
  private void editarArvore(Map<String,Object> listaAtualizar, JsonNode obj, StringBuilder builder){
    Iterator<String> nodeNameIt = obj.fieldNames();
    while (nodeNameIt.hasNext()) {
      String nodeName = nodeNameIt.next();
      JsonNode node = obj.get( nodeName );
      
      StringBuilder newBuilder = new StringBuilder(30);
      if( builder != null ) newBuilder.append(builder);
      if( newBuilder.length() > 0 ) newBuilder.append(".");
      newBuilder.append(nodeName);
      
      if( node.getNodeType() == JsonNodeType.OBJECT ){
        editarArvore( listaAtualizar, node, newBuilder );
        continue;
      }
      Object value = null;

      switch (node.getNodeType()) {
        case BOOLEAN:
          value = node.asBoolean();
          break;
        case STRING:
          value = node.asText();
          break;
        case NUMBER:
          value = node.isDouble()
                  ? node.asDouble() : node.asInt();
          break;
      }

      if (value == null && !node.getNodeType().equals(JsonNodeType.NULL)) {
        continue;
      }
      
      listaAtualizar.put( newBuilder.toString(), value );
    }
  }

  public int deletar(BuscaInfo info) {
    Class<?> klass = cache.get(info.entidade, em);
    checker.check(klass, Seguranca.DELETE);
    
    // ----  Criando a busca ao banco:
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaDelete query = cb.createCriteriaDelete(klass);
    Root root = query.from(klass);

    // Cláusula WHERE do banco:
    Predicate[] preds = WhereBuilder.build(cb, root, info.query);
    if (preds.length < 1) {
      throw new MsgException(JsonResponse.ERROR_EXCECAO,null,"Os parâmetros de filtragem da QueryString não são válidos.");
    }
    query.where(preds);

    checker.checkPersistencia(klass, cb, query);

    // A busca ao banco:
    int qtd = em.createQuery(query).executeUpdate();
    return qtd;
  }
  
  public void adicionar(String entidade, BuscaInfo info){
    
  }
  
  public void remover(){
    
  }
  
  //============================================================================
  
  public void addOrderBy(CriteriaBuilder cb, CriteriaQuery query, String[] orders) {
    if (orders.length < 1) {
      return;
    }
    Root root = (Root) query.getRoots().iterator().next();
    List<Order> lista = new ArrayList<>(6);
    for (String s : orders) {
      try {
        String[] ordersOrder = s.trim().split("\\s+");
        if (ordersOrder.length > 1 && ordersOrder[1].matches("(?i)desc")) {
          lista.add(cb.desc(root.get(ordersOrder[0])));
        } else {
          lista.add(cb.asc(root.get(ordersOrder[0])));
        }
      } catch (IllegalArgumentException ex) {
      }
    }
    query.orderBy(lista);
  }
  
  
  /*
  private static final Map<Class,Integer> mapConvercao = new HashMap<>(20);
  static{
    // 0: byte
    // 1: int
    // 2: long
    // 3: double
    // 4: datas -> String para interpretar, usando padrão ISO
    mapConvercao.put( Boolean.class, 0);
    mapConvercao.put( boolean.class, 0);
    mapConvercao.put( Byte.class, 1);
    mapConvercao.put( byte.class, 1);
    mapConvercao.put( Short.class, 1);
    mapConvercao.put( short.class, 1);
    mapConvercao.put( Integer.class, 1);
    mapConvercao.put( int.class, 1);
    mapConvercao.put( Long.class, 2);
    mapConvercao.put( long.class, 2);
    mapConvercao.put( BigInteger.class, 2);
    mapConvercao.put( Float.class, 3);
    mapConvercao.put( float.class, 3);
    mapConvercao.put( Double.class, 3);
    mapConvercao.put( double.class, 3);
    mapConvercao.put( BigDecimal.class, 3);
    mapConvercao.put( Date.class, 4);
    mapConvercao.put( Time.class, 4);
    mapConvercao.put( Calendar.class, 4);
  }
  private Object as( JsonNode node, Class<?> tipo ){
    if( tipo == null || node == null ) return null;
    if( Integer.class.isAssignableFrom(tipo) || int.class.isAssignableFrom(tipo) ) return node.asInt();
    if( Long.class.isAssignableFrom(tipo) ) return node.asLong();
    if( Double.class.isAssignableFrom(tipo) ) return node.asDouble();
    if( Number.class.isAssignableFrom(tipo) ) return node.asLong();
    if( String.class.isAssignableFrom(tipo) ) return node.asText();
    if( Date.class.isAssignableFrom(tipo) ) throw new MsgException(JsonResponse.ERROR_EXCECAO,null,"Fazer impl. de Json para Date");
    
    return null;
  }
  /* */
  
}
