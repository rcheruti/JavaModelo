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
import javax.persistence.metamodel.SingularAttribute;
import javax.ws.rs.Consumes;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/persistencia/um/id/{entidade}")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesUmIdService {

  private final static Map<String, Runnable> map = new HashMap<>();

  private final int pageNumDefault = 0;
  private final int pageSizeDefault = 20;

  @Inject
  private EntityManager em;
  @Inject
  private ClassCache cache;
  @Inject
  private SegurancaServico checker;
  
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
  @POST
  @Path("/buscar")
  public JsonResponse buscar(
          @PathParam("entidade") String entidade,
          // 'Pagina' da busca, cláusula OFFSET do banco
          @MatrixParam("page") String pageNumStr,
          // Quantidade de itens na página, cláusula LIMIT do banco
          @MatrixParam("size") String pageSizeStr,
          // Itens relacionados que devem ser carregados, cláusula JOIN FETCH da JPQL
          @MatrixParam("join") String joinMatrix,
          @MatrixParam("order") String orderMatrix,
          JsonNode node
  ) {
    Class<?> klass = cache.get(entidade, em);
    if (klass == null) {
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade));
    }
    checker.check(klass, Seguranca.SELECT);
    
    // Pegando os ids da classe
    Set<SingularAttribute> ids = PersistenciaUtils.getIds(em, klass);
    if( ids == null ){
      System.out.printf("---  Ids eh NULL \n");
    }else{
      System.out.printf("---  ids.size: \n", ids.size() );
      for( SingularAttribute attr : ids ){
        System.out.printf("---  - ID: %s   \n", attr.getName() );
      }
    }
    if (ids == null || ids.isEmpty()) {
      return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
    }
    
    // O JSON deve ser um Array, com as entidades dentro
    if( node == null || !node.isArray() ){
      return new JsonResponse(false,"A mensagem deve ser um Array JSON.");
    }
    
    List resposta = new ArrayList<>();
    List<SingularAttribute> idLista = new ArrayList<>();
    List<Predicate> wheres = new ArrayList<>();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    
    iteracaoObjs:
    for(JsonNode json : node){
      if( json == null || !json.isObject() ) continue;
      idLista.clear();
      wheres.clear();
      
      CriteriaQuery query = cb.createQuery();
      Root root = query.from( klass );
      query.select(root);
      
      iteracaoIds:
      for( SingularAttribute idAttr : ids ){
        JsonNode prop = json.get(idAttr.getName());
        if( prop == null /* ... comp do tipo do ID */ ) continue;
        idLista.add(idAttr);
        System.out.printf("---  Root.get: %s, prop.VALOR: %s, idAttr.getJavaType: %s \n", 
                idAttr.getName(), as(prop, idAttr.getJavaType()), idAttr.getJavaType() );
        wheres.add( cb.equal( root.get(idAttr.getName()) , as(prop, idAttr.getJavaType()) ) );
      }
      if( wheres.isEmpty() ){
        System.out.printf("---  wheres.isEmpty \n");
        continue;
      }
      
      query.where(wheres.toArray(new Predicate[0]));
      List lista = em.createQuery(query).setMaxResults(1).getResultList();
      if( lista != null ) for( Object x : lista ) resposta.add(x);
      
    }
    
    return new JsonResponse(true, resposta, "Busca por IDs");
  }
  
  public JsonResponse editar(
          @PathParam("entidade") String entidade,
          JsonNode obj
  ) {
    Class<?> klass = cache.get(entidade, em);
    if (klass == null) {
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade));
    }
    checker.check(klass, Seguranca.UPDATE);
    
    if( obj == null ){
      return new JsonResponse(false, "É necessário informar os dados da persistência");
    }
    if( !obj.isArray() ){
      return new JsonResponse(false, "Os dados devem ser objetos dentro de um Array");
    }
        
        // Pegando os ids da classe
        Set<SingularAttribute> ids = PersistenciaUtils.getIds(em, klass);
        if (ids == null || ids.isEmpty()) {
          return new JsonResponse(false, "Não encontramos os campos de Id dessa classe");
        }
        
        for( JsonNode node : obj ){
          
            // ----  Criando a busca ao banco:
          CriteriaBuilder cb = em.getCriteriaBuilder();
          CriteriaUpdate query = cb.createCriteriaUpdate(klass);
          Root root = query.from( klass );
          
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
