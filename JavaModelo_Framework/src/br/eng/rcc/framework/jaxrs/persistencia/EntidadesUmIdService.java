package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/persistencia/um/id/{entidade}")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesUmIdService {

  @Inject
  protected EntityManager em;
  @Inject
  protected ClassCache cache;
  @Inject
  protected SegurancaServico checker;
  @Inject
  protected EntidadesService entService;
    
  
  /**
   * Para que este objeto possa fazer o seu trabalho, é obrigatório um
   * {@link EntityManager EntityManager} para acessar o banco.
   */
  @PostConstruct
  public void postConstruct() {
    if (em == null) {
      String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
      throw new MsgException(msg);
    }
  }
  
  //=====================================================================
  
  @GET @Path("/tipo")
  public JsonResponse tipo(
          @PathParam("entidade") String entidade
    ){
    Class<?> klass = cache.get(entidade, em);
    if( klass == null ){
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
    }
    return entService.tipo(klass);
  }
  
  
  @POST
  @Path("/buscar")
  @Transactional
  public JsonResponse buscar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx,
          JsonNode node
  ) {
    
    // O JSON deve ser um Array, com as entidades dentro
    if( node == null || !node.isArray() ){
      return new JsonResponse(false,"A mensagem deve ser um Array JSON.");
    }
    
    Class<?> klass = cache.get(entidade, em);
    if (klass == null) {
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade));
    }
    checker.check(klass, Seguranca.SELECT);
    
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( ctx.getPath() );
    
    // Pegando os ids da classe
    List<String> ids = PersistenciaUtils.getIds(em, klass);
    if (ids == null || ids.isEmpty()) {
      return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
    }
    
    
    List resposta = new ArrayList<>();
    List<Predicate> wheres = new ArrayList<>();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    
    iteracaoObjs:
    for(JsonNode json : node){
      if( json == null || !json.isObject() ) continue;
      wheres.clear();
      
      CriteriaQuery query = cb.createQuery();
      Root root = query.from( klass );
      query.select(root);
      
      for( String idAttr : ids ){
        String[] idS = idAttr.split("\\.");
        JsonNode prop = json;
        for( String s : idS ) prop = prop.get(s);
        if( prop == null /* ... comp do tipo do ID */ ) continue;
        javax.persistence.criteria.Path exp = root.get( idS[ 0 ] );
        for( int i = 1; i < idS.length; i++ ) exp = exp.get( idS[i] );
        wheres.add( cb.equal( exp , as(prop, exp.getJavaType() ) ) );
      }
      if( wheres.isEmpty() ){
        continue;
      }
      query.where(wheres.toArray(new Predicate[0]));
      
      List lista = em.createQuery(query)
              .setFirstResult( info.getSize() * info.getPage() )
              .setMaxResults( info.getSize() )
              .getResultList();
      if( lista != null ){
        PersistenciaUtils.resolverLazy(cache, lista.toArray(), false, info.getJoin() ); 
        em.clear();
        PersistenciaUtils.resolverLazy(cache, lista.toArray(), true,  info.getJoin() ); 
        for( Object x : lista ) resposta.add(x);
      }else{
        em.clear();
      }
      
    }
    
    return new JsonResponse(true, resposta, "Busca por IDs");
  }
  
  public JsonResponse editar(
          @PathParam("entidade") String entidade,
          JsonNode json
  ) {
    Class<?> klass = cache.get(entidade, em);
    if (klass == null) {
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade));
    }
    checker.check(klass, Seguranca.UPDATE);
    
    if( json == null ){
      return new JsonResponse(false, "É necessário informar os dados da persistência");
    }
    if( !json.isArray() ){
      return new JsonResponse(false, "Os dados devem ser objetos dentro de um Array");
    }
        
    // Pegando os ids da classe
    List<String> ids = PersistenciaUtils.getIds(em, klass);
    if (ids == null || ids.isEmpty()) {
      return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
    }

    List<Predicate> wheres = new ArrayList<>();
    CriteriaBuilder cb = em.getCriteriaBuilder();

    for( JsonNode node : json ){
        // ----  Criando a busca ao banco:
      CriteriaUpdate query = cb.createCriteriaUpdate(klass);
      Root root = query.from( klass );
      
      int xSplit = 0;
      String[][] idsSplit = new String[ ids.size() ][];
      
      for( String idAttr : ids ){
        String[] idS = idsSplit[ xSplit++ ] = idAttr.split("\\.");
        JsonNode prop = node;
        for( String s : idS ) prop = prop.get(s);
        if( prop == null /* ... comp do tipo do ID */ ) continue;
        javax.persistence.criteria.Path exp = root.get( idS[ 0 ] );
        for( int i = 1; i < idS.length; i++ ) exp = exp.get( idS[i] );
        wheres.add( cb.equal( exp , as(prop, exp.getJavaType() ) ) );
      }
      if( wheres.isEmpty() ){
        continue;
      }
      query.where(wheres.toArray(new Predicate[0]));
      
      for( JsonNode attrNode : node ){
          // ainda falta adicionar a parte dessa linha abaixo!
        if( attrNode.isArray() || attrNode.isObject() ) continue;
        
        
      }
      query.set(root, cache);
    }
    
    return null;
  }
  
  //===============  Privates  ==================
  private void addOrderBy(CriteriaBuilder cb, CriteriaQuery query, String[] orders) {
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
        /*
                Throwable ttt = ex;
                Throwable lastttt = null; // Para proteger de loop infinito
                while( ttt != null && ttt != lastttt ){
                    System.out.printf("---   Ex.: %s \n", ttt.getMessage() );
                    lastttt = ttt;
                    ttt = ttt.getCause();
                }
                /* */
      }
    }
    query.orderBy(lista);
  }
  
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
    if( Date.class.isAssignableFrom(tipo) ) throw new MsgException("Fazer impl. de Json para Date");
    
    return null;
  }
  
  
}
