package br.eng.rcc.framework.persistencia;

import br.eng.rcc.framework.utils.ClassCache;
import br.eng.rcc.framework.jaxrs.JacksonOM;
import br.eng.rcc.framework.persistencia.builders.WhereBuilder;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.utils.ClasseAtributoUtil;
import br.eng.rcc.framework.utils.Busca;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javax.transaction.Transactional;

@ApplicationScoped
public class EntidadesService {
  
  @Inject
  protected EntityManager em;
  @Inject
  protected ClassCache cache;
  @Inject
  protected SegurancaServico checker;
  @Inject
  private JacksonOM resolver;
  
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
/**
   * Este é o serviço princiopal, daqui a busca será encaminhada para o método 
   * correto de acordo com os parâmetros do objeto {@link Busca}.
   * <br><br>
   * 
   * @param busca Informações da busca que será executada
   * @param scope O Escope de busca para processar a busca (pode ser null)
   * @return Object O resultado da resposta. Pode ser apenas um objeto ou uma lista
   * @throws Exception 
   */
  @Transactional
  public Object processar(Busca busca, BuscaReqScope scope) throws Exception{
    Object ooo = null;
    if( scope == null ) scope = new BuscaReqScope();
    
    switch( busca.acao ){
      case Busca.ACAO_TIPO:
        busca.chaves = false;
        ooo = chamarBusca( busca, scope, this::tipo ).get();
        break;
      case Busca.ACAO_BUSCAR:
        ooo = chamarBusca( busca, scope, this::buscar ).get();
        break;
      case Busca.ACAO_CRIAR:
        if( busca.chaves ){
          throw new MsgException("Não é permitido criar entidades a partir do ID");
        }
        ooo = chamarBusca( busca, scope, this::criar ).get();
        break;
      case Busca.ACAO_EDITAR:
        ooo = chamarBusca( busca, scope, this::editar ).get();
        break;
      case Busca.ACAO_DELETAR:
        ooo = chamarBusca( busca, scope, this::deletar ).get();
        break;
      case Busca.ACAO_ADICIONAR:
        //throw new MsgException("Ainda não existe impl. para ADICIONAR");
        ooo = chamarBusca( busca, scope, this::adicionar ).get();
        break;
      case Busca.ACAO_REMOVER:
        //throw new MsgException("Ainda não existe impl. para REMOVER");
        ooo = chamarBusca( busca, scope, this::remover ).get();
        break;
      case Busca.ACAO_PAGINAR:
        busca.chaves = false;
        ooo = chamarBusca( busca, scope, this::paginas ).get();
        break;
      case Busca.ACAO_REFS:
        if( busca.data != null )
          for(JsonNode node : busca.data){
            Iterator<String> nomes = node.fieldNames();
            while(nomes.hasNext()){
              String n = nomes.next();
              scope.refs.put(n, node.get(n) );
            }
          }
        break;
    }
    
    String key = busca.id;
    if( key == null ) key = "1";
    scope.resps.put(key, ooo);
    return ooo; 
  }
  
  @Transactional
  public List<Object> processar(Collection<Busca> buscas, BuscaReqScope scope) {
    List<Object> resposta = new ArrayList<>( buscas.size() );
    Object ooo = null;
    if( scope == null ) scope = new BuscaReqScope();
    int keyInt = 1;
    
    for( Busca busca : buscas ){
      try{
        if( busca.id == null ) busca.id = ""+ keyInt++;
        ooo = processar(busca, scope);
        resposta.add(ooo);
      }catch(MsgException ex){
        resposta.add( new JsonResponse(false, ex.getCodigo(),
                ex.getData(), ex.getMensagem()) );
      }catch(Exception ex){
        resposta.add( new JsonResponse(false, JsonResponse.ERROR_DESCONHECIDO,
                null, "Exceção inesperada: "+ ex.getMessage() ) );
        ex.printStackTrace();
      }
    }
    return resposta;
  }
  
  //=====================================================================
  
  public Map<String, String> tipo( Busca info, BuscaReqScope scope ) {
    checker.check(info.classe, Seguranca.SELECT | Seguranca.INSERT
            | Seguranca.DELETE | Seguranca.UPDATE);

    Metamodel meta = this.em.getMetamodel();
    ManagedType entity = meta.managedType(info.classe);

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
  
  public int paginas(Busca info, BuscaReqScope scope){
    checker.check( info.classe, Seguranca.SELECT );
    if( info.size == 0 ) return 0;
    
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery query = cb.createQuery( Long.class );
    Root root = query.from( info.classe );
    query.select( cb.count(root) );
    
    // Cláusula WHERE do banco:
    query.where( WhereBuilder.build(cb, root, info.where) );
    
    // A busca ao banco:
    Query q = em.createQuery(query);
    
    // O resultado
    List<Long> res = q.getResultList();
    
    return (int)Math.ceil( res.get(0).doubleValue() / info.size );
  }
  
  @Transactional
  public List<Object> buscar(Busca info, BuscaReqScope scope) {
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
    query.where( WhereBuilder.build(cb, root, info.where) );

    // A busca ao banco:
    Query q = em.createQuery(query);
    q.setFirstResult( info.page * info.size );
    q.setMaxResults( info.size );
    
    // O resultado
    List<Object> res = q.getResultList();
    PersistenciaUtils.resolverLazy(cache, res.toArray(), false, info.join.toArray(new String[0]) );
    this.em.clear();
    PersistenciaUtils.resolverLazy(cache, res.toArray(), true, info.join.toArray(new String[0]) );

    return res;
  }
  
  @Transactional
  public List<Object> criar(Busca info, BuscaReqScope scope){
    checker.check(info.classe, Seguranca.INSERT);
    List<Object> objs = new ArrayList<>();
    
    try{
      for( JsonNode node : info.data ){
          // Aqui tem uma verificação de referencias!!!
        objs.add( mapper.treeToValue( node, info.classe) );
      }
    }catch( JsonProcessingException ex ){
      throw new RuntimeException("JsonProcessingException atirada!", ex);
    }
    
    // precisamos colocar o objeto no lado inverso da relação para que tudo entre
    // no banco com os valores corretos
    Map<String, ClasseAtributoUtil> map = cache.getInfo(info.from );
    try {
      for (ClasseAtributoUtil util : map.values()) {
        if (util.isAssociacao()) {
          ClasseAtributoUtil inverso = util.getInverse();
          if (inverso == null) {
            continue;
          }
          if (util.isColecao()) {
            for (Object obj : objs) {
              if( util.get(obj) == null ) continue;
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
              if( util.get(obj) == null ) continue;
              inverso.set(util.get(obj), obj);
            }
          }
        }
      }
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new RuntimeException("Problemas de Introspecção ou Reflexão ao criar entidades", ex);
    }
    for (Object obj : objs) {
      em.persist(obj);
    }
    this.em.flush();
    PersistenciaUtils.resolverLazy(cache, objs.toArray(), false, info.join.toArray(new String[0]) );
    this.em.clear();
    PersistenciaUtils.resolverLazy(cache, objs.toArray(), true, info.join.toArray(new String[0]) );
    
    return objs;
  }
  
  @Transactional
  public int editar(Busca info, BuscaReqScope scope) {
    checker.check(info.classe, Seguranca.UPDATE);
    
    Map<String,Object> listaAtualizar = new HashMap<>();
    editarArvore(listaAtualizar, info.data.path(0), null);
    
    // ----  Criando a busca ao banco:
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaUpdate query = cb.createCriteriaUpdate(info.classe);
    Root root = query.from(info.classe);
    
    
    
    // Cláusula WHERE do banco:
    Predicate[] preds = WhereBuilder.build(cb, root, info.where);
    if (preds == null || preds.length < 1) {
      throw new MsgException(JsonResponse.ERROR_EXCECAO,null,"Os parâmetros de filtragem da QueryString não são válidos.");
    }
    query.where(preds);
    
    List<String> ids = PersistenciaUtils.getIds(em, info.classe);
    
    atualizar:
    for( String entryName : listaAtualizar.keySet() ){
      for(String sss : ids) if( entryName.equals(sss) ){
        continue atualizar;
      }
      String[] listaStr = entryName.split("\\.");
      if( listaStr.length < 1 ) continue;
      Path exp = root.get( listaStr[0] );
      for( int i = 1; i < listaStr.length; i++ ) exp = exp.get( listaStr[i] );
      
      Object valor = listaAtualizar.get( entryName );
        if( valor == null ){
          // não vamos fazer nada aqui
        }else if( exp.getJavaType().equals(Date.class) ){
          valor = javax.xml.bind.DatatypeConverter.parseDateTime( (String)valor ).getTime();
        }else if( exp.getJavaType().equals(Calendar.class) ){
          valor = javax.xml.bind.DatatypeConverter.parseDateTime( (String)valor );
        }else if( exp.getJavaType().equals(Time.class) ){
          Date d = javax.xml.bind.DatatypeConverter.parseDateTime( (String)valor ).getTime();
          valor = new Time( d.getTime() );
        }
      query.set(exp, valor );
    }
    

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

  @Transactional
  public int deletar(Busca info, BuscaReqScope scope) {
    checker.check( info.classe, Seguranca.DELETE);
    
    // ----  Criando a busca ao banco:
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaDelete query = cb.createCriteriaDelete( info.classe );
    Root root = query.from( info.classe );

    // Cláusula WHERE do banco:
    Predicate[] preds = WhereBuilder.build(cb, root, info.where);
    if (preds.length < 1) {
      throw new MsgException(JsonResponse.ERROR_EXCECAO,null,"Os parâmetros de filtragem da QueryString não são válidos.");
    }
    query.where(preds);


    // A busca ao banco:
    int qtd = em.createQuery(query).executeUpdate();
    return qtd;
  }
  
  @Transactional
  public int adicionar(Busca info, BuscaReqScope scope) {
    
    Map<String, ClasseAtributoUtil> map = cache.getInfo(info.from );
    int adds = 0;
    try{
      List<Object> oooS = this.buscar(info, scope);
      for( JsonNode node : info.data ){
        
        Iterator<String> itKeys = node.fieldNames();
        while( itKeys.hasNext() ){
          String key = itKeys.next();
          ClasseAtributoUtil util = map.get(key);
          if( util.isAssociacao() ){
            if( util.isColecao() ){
              for( JsonNode n : node.path(key) ){
                Object objX = mapper.treeToValue(n, util.getJavaType());
                for( Object x : oooS ) util.add(x, objX);
                adds += oooS.size();
              }
            }else{
              Object objX = mapper.treeToValue(node.path(key), util.getJavaType());
              for( Object x : oooS ) util.set(x, objX);
              adds += oooS.size();
            }
          }
        }
      }
      for(Object x : oooS) em.merge( x );
    } catch (IllegalAccessException | JsonProcessingException |
            InvocationTargetException ex) {
      throw new RuntimeException("Problemas de Introspecção ou Reflexão ao criar entidades", ex);
    }
    return adds;
  }
  
  @Transactional
  public int remover(Busca info, BuscaReqScope scope){
    throw new MsgException("Ainda não existe impl. para REMOVER");
  }
  
  //============================================================================
  
  public void addOrderBy(CriteriaBuilder cb, CriteriaQuery query, List<String> orders) {
    if (orders.size() < 1) {
      return;
    }
    Root root = (Root) query.getRoots().iterator().next();
    List<Order> lista = new ArrayList<>(6);
    for (String s : orders) {
      try {
        String[] ordersOrder = s.trim().split("\\s+");
        String[] listaProps = ordersOrder[0].split("\\.");
        Path path = root.get(listaProps[0]);
        for(int i = 1; i < listaProps.length; i++) path = path.get( listaProps[i] );
        
        if (ordersOrder.length > 1 && ordersOrder[1].matches("(?i)desc")) {
          lista.add(cb.desc( path ));
        } else {
          lista.add(cb.asc( path ));
        }
      } catch (IllegalArgumentException ex) {
      }
    }
    query.orderBy(lista);
  }
  
  private Optional chamarBusca( Busca busca, BuscaReqScope scope, IChamadaBusca metodo ){
    Object ooo = null;
    
    checker.check(busca.classe, 
              busca.acao == Busca.ACAO_BUSCAR ? Seguranca.SELECT
              : busca.acao == Busca.ACAO_CRIAR ? Seguranca.INSERT
              : busca.acao == Busca.ACAO_EDITAR ? Seguranca.UPDATE
              : busca.acao == Busca.ACAO_DELETAR ? Seguranca.DELETE
                      : 0
            );
    
    if( busca.chaves ){
      List lista = new ArrayList<>(); // para resposta em lista ;
      int resp = 0; // para respostas em número
      
      List<String> ids = PersistenciaUtils.getIds(em, busca.classe);
      if (ids == null || ids.isEmpty()) {
        throw new MsgException(String
          .format("Não encontramos os campos de Id dessa classe: '%s'", busca.from));
      }
      
      ArrayNode dataArray = busca.data;
      for( JsonNode node : dataArray ){
        if( node == null || !node.isObject() ) continue;
        busca.where = new ArrayList<>( ids.size() + 1 );
        for( String idAttr : ids ){
          String[] idS = idAttr.split("\\.");
          JsonNode prop = node;
          for( String s : idS ) prop = prop.get(s);
          if( prop == null ) continue;
          busca.where.add( new String[]{ idAttr, prop.isNull()?"isnull":"=", prop.asText(), "&" } );
        }
        
        ArrayNode arr = new ArrayNode(JsonNodeFactory.instance);
        arr.add(node);
        busca.data = arr ;
        
        checker.filterPersistencia(busca);
        ooo = metodo.chamarBusca(busca,scope);
        if( ooo instanceof List ){
          checker.filterPersistencia(busca, (List)ooo );
          for( Object x : (List)ooo ) lista.add(x);
        }else{
          checker.filterPersistencia(busca, Arrays.asList( ooo ) );
          resp += (int)ooo;
        }
      }
      if( !lista.isEmpty() ) ooo = lista;
      else ooo = resp;
    }else{
      checker.filterPersistencia(busca);
      ooo = metodo.chamarBusca(busca,scope);
      if( ooo instanceof List ){
        checker.filterPersistencia(busca, (List)ooo );
      }else{
        checker.filterPersistencia(busca, Arrays.asList( ooo ) );
      }
    }
    return Optional.of( ooo );
  }
  
  private static interface IChamadaBusca {
    Object chamarBusca( Busca busca, BuscaReqScope scope );
  }
  
}
