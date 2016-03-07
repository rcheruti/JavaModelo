
package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.jaxrs.persistencia.builders.WhereBuilderInterface;
import br.eng.rcc.framework.jaxrs.persistencia.builders.WhereBuilder;
import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;


@Path("/persistencia/um/{entidade}")
@Produces({ Configuracoes.JSON_PERSISTENCIA })
@Consumes({ Configuracoes.JSON_PERSISTENCIA })
@RequestScoped
public class EntidadesUmService {
    
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
    public void postConstruct(){
        if( em == null ){
            String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
            throw new MsgException(msg);
        }
    } 
    
        
    //=============================================================================
    //=============================================================================
    //=============================================================================
    
    /*
        Abaixo estão os métodos que trabalharão com apenas 1 tipo de entidade por vez
    */
    
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
    
    
    /** 
     * Exemplo de URL para abusca:
     * "http://www.endereco.com.br/jaxRootContext/persistence/MinhaClasse;matrix1=valor;matrix2=valor?attr1=valor & attr2 != valor2"
     * 
     * Ex.:
     * ".../persistence/Comprador;join=carros;order=nome;page=3;size=30? idade > 18"
     * Retornará as entidades da classe "Comprador" que tenham o atributo "idade" (a classe DEVE ter este atributo) maior que 18,
     * ordenados pelo "nome", sendo que a busca retornará a página 4 (pois "page" inicia em 0) limitada em 30 itens.
     * 
     * Parâmetros da matrix:
     *  page: Este parâmetro informa a página que deverá ser retornada (cláusula OFFSET), iniciando em 0.
     *  size: Este parâmetro informa a quantidade de itens que retornarão por página (cláusula LIMIT).
     *  join: Este parâmetro informa quais atributos devemos incluir no "FETCH JOIN" (cláusulas JOIN).
     *  order: Este parâmetro informa quais atributos serão usados para a ordenação (cláusula ORDER BY).
     * 
     * @param entidade Nome simples da classe que iremos buscar uma entidade.
     * @param ctx Este parâmetro será informado pelo JAX-RS, daqui pegaremos a "Query String"
     * que será usada para fazer a filtragem das entidades (cláusula WHERE).
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @POST @Path("/buscar")
    public JsonResponse buscar(
                // Nome da classe (entidade) que iremos usar como parâmetro principal
            @PathParam("entidade") String entidade ,
                // Apenas para coletarmos dados da requisição, não vem da URL
            @Context UriInfo ctx
        ){
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
        }
        checker.check( klass, Seguranca.SELECT );
        
        
        PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( ctx.getPath() );
        
        
        // ----  Criando a busca ao banco:
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery query = cb.createQuery();
        Root root = query.from( klass );
        query.select(root);
        
        // Cláusula JOIN FETCH da JPQL:
        for(String s : info.getJoin()){ 
          if( s.indexOf('.') < 0 )
            root.fetch(s, JoinType.LEFT);
        }
        
        // Cláusula ORDER BY da JPQL:
        addOrderBy( cb, query, info.getOrder() );
        
        
        // Cláusula WHERE do banco:
        WhereBuilderInterface wb = WhereBuilder.create(cb, query);
        query.where( wb.addArray( info.getQuery() ).build() );
        
        checker.checkPersistencia(klass, cb, query);
        
        // A busca ao banco:
        Query q = em.createQuery(query);
        q.setFirstResult( info.getPage() * info.getSize() );
        q.setMaxResults( info.getSize() );
        List<Object> res = q.getResultList();
        PersistenciaUtils.resolverLazy(cache, res.toArray(), false, info.getJoin() );
        this.em.clear();
        PersistenciaUtils.resolverLazy(cache, res.toArray(), true, info.getJoin() );

        // A resposta: 
        return new JsonResponse(true,res,"Lista dos objetos", info.getPage(), info.getSize());
        
    }
    
    
    /**
     * Este método deve receber no "PathParam" o nome simples da classe que iremos criar
     * uma nova entidade no banco de dados.
     * 
     * Os dados dessa entidade devem ser enviados no corpo do POST, no formato JSON.
     * Atributos que não estejam presentes receberão o seu valor padrão (valor padrão da instrução "new"),
     * e atributos que não pertençam a esta entidade serão ignorados.
     * 
     * - Para gravar relações "OneToOne" é necessário adicionar "cascade = PERSIST/ALL".
     * - Para gravar relações "ManyToMany" não é permitido adicionar "cascade = PERSIST/ALL"
     * na relação.
     * - Para gravar relações "OneToMany" é necessário adicionar a todos os itens do 
     * outro lado da relação (que terá uma coleção por ser "ManyToOne") uma referência
     * a este objeto. Isso pode ser feito no método GET.
     * Necessário adicionar "cascade = PERSIST/ALL".
     * - - Atenção: caso esteja usando Lombok a persistência pode dispara um loop infinito
     * por causa dos métodos "toString", "hascode" ou "equals". Caso veja "StackOverflowError" no 
     * console verifique esses métodos.
     * 
     * 
     * @param objs Lista de objetos para persistência no banco de dados
     * @param entidade Nome da entidade de banco de dados
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @POST @Path("/")
    @Transactional
    public JsonResponse criar(List<?> objs, @PathParam("entidade") String entidade){
        if( objs == null || objs.isEmpty() ){
            return new JsonResponse(false,
                String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
        }
        Class klass = objs.get(0).getClass();
        checker.check( klass, Seguranca.INSERT );
        
          // precisamos colocar o objeto no lado inverso da relação para que tudo entre
          // no banco com os valores corretos
        Map<String,ClassCache.BeanUtil> map = cache.getInfo(entidade);
        try{
          for( ClassCache.BeanUtil util : map.values() ){
            if( util.isAssociacao() ){
              ClassCache.BeanUtil inverso = util.getInverse();
              if( inverso == null ) continue;
              if( util.isColecao() ){
                for( Object obj : objs ){
                  Collection coll = ((Collection)util.get(obj));
                  if( coll == null ) continue;
                  for( Object ooo : coll ){
                    inverso.set( ooo , obj);
                  }
                }
              }
              else for( Object obj : objs ) inverso.set( util.get(obj) , obj);
            }
          }
        }catch(IllegalAccessException | InvocationTargetException ex){
          throw new RuntimeException("Problemas de Introspecção ou Reflexão ao criar entidades", ex);
        }
        for( Object obj : objs ){
          checker.checkPersistencia(klass, obj);
          em.persist(obj);
        }
        
        return new JsonResponse(true,null,null );
    }
    
    
    /**
     * Este método deve receber no "PathParam" o nome simples da classe que iremos editar
     * uma entidade no banco de dados.
     * 
     * Na "Query String" deve ser informado os atributos de filtragem que devemos usar para encontrar
     * esta entidade na base. A "Query String" <strong>é obrigatória!</strong>
     * <i>Não é permitido atualizar todos os registros do banco de dados, mas é 
     * possível atualizar vários.</i>
     * 
     * Os dados que serão atualizados devem ser passados no corpo do PUT.
     * Não é permitido informar atributos que não pertençam a esta entidade, caso isso 
     * aconteça um erro de atualização (UPDATE) acontecerá e nada será atualizado.
     * 
     * @param entidade
     * @param ctx
     * @param obj
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @PUT @Path("/")
    @Transactional
    public JsonResponse editar(
            @PathParam("entidade") String entidade,
            @Context UriInfo ctx,
            JsonNode obj){ // JsonNode
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
        }
        checker.check( klass, Seguranca.UPDATE);
        
        
        String uriQuery = ctx.getRequestUri().getQuery();
        if( uriQuery == null || uriQuery.isEmpty() ){
            return new JsonResponse(false,
                "Para editar registros é necessário informar os parâmetros de filtragem na QueryString.");
        }
        String[][] querysPs = PersistenciaUtils.parseQueryString(uriQuery);
        
        
        // ----  Criando a busca ao banco:
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate query = cb.createCriteriaUpdate(klass);
        Root root = query.from( klass );
        
         // Cláusula WHERE do banco:
        WhereBuilderInterface wb = WhereBuilder.create(cb, query);
        Predicate[] preds = wb.addArray( querysPs ).build();
        if( preds.length < 1 ){
           return new JsonResponse(false,"Os parâmetros de filtragem da QueryString não são válidos."); 
        }
        query.where( preds );
        
        // Montando o update, excluindo os atributos usados na query string:
        Iterator<String> nodeNameIt = obj.fieldNames();
        String[] querysPsNames = new String[ querysPs.length ];
        for( int i = 0; i < querysPs.length; i++ )
            querysPsNames[i] = querysPs[i][0];
        while( nodeNameIt.hasNext() ){
            String nodeName = nodeNameIt.next();
            if( PersistenciaUtils.constainsInArray(querysPsNames, nodeName) ) continue;
            try{
                JsonNode node = obj.get(nodeName);
                Object value = null;
                
                switch( node.getNodeType() ){ 
                    case BOOLEAN: value = node.asBoolean(); break;
                    case STRING: value = node.asText(); break;
                    case NUMBER: value = node.isDouble()? 
                            node.asDouble():node.asInt() ; break;
                }
                
                if( value == null 
                    && !node.getNodeType().equals(JsonNodeType.NULL) ) continue;
                query.set( root.get(nodeName), value );
            }catch(Exception ex){  }
        }
        
        checker.checkPersistencia(klass, cb, query);
        
        int ups = em.createQuery(query).executeUpdate();
        
        return new JsonResponse(true,ups, null);
    }
    
    
    /**
     * Este método deve receber no "PathParam" o nome simples da classe que iremos deletar
     * uma entidade no banco de dados.
     * 
     * Na "Query String" deve ser informado os atributos de filtragem que devemos usar para encontrar
     * esta entidade na base. A "Query String" <strong>é obrigatória!</strong>
     * <i>Não é permitido deletar todos os registros do banco de dados, mas é 
     * possível deletar vários.</i>
     * 
     * 
     * @param entidade
     * @param ctx
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @DELETE @Path("/")
    @Transactional
    public JsonResponse deletar(
                @PathParam("entidade") String entidade ,
                @Context UriInfo ctx
            ){
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
        }
        checker.check( klass, Seguranca.DELETE);
        
        
        String uriQuery = ctx.getRequestUri().getQuery();
        if( uriQuery == null || uriQuery.isEmpty() ){
            return new JsonResponse(false,
                "Para apagar registros é necessário informar os parâmetros de filtragem na QueryString.");
        }
        String[][] querysPs = PersistenciaUtils.parseQueryString(uriQuery);
        
        
        
        // ----  Criando a busca ao banco:
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete query = cb.createCriteriaDelete(klass);
        Root root = query.from( klass );
        
        
        // Cláusula WHERE do banco:
        WhereBuilderInterface wb = WhereBuilder.create(cb, query);
        Predicate[] preds = wb.addArray( querysPs ).build();
        if( preds.length < 1 ){
           return new JsonResponse(false,"Os parâmetros de filtragem da QueryString não são válidos."); 
        }
        query.where( preds );
        
        checker.checkPersistencia(klass, cb, query);
        
        // A busca ao banco:
        int qtd = em.createQuery(query).executeUpdate();
        
        return new JsonResponse(true,qtd,null);
    }
    
    
    
    //===============  Privates  ==================
    private void addOrderBy(CriteriaBuilder cb, CriteriaQuery query, String[] orders){
        if( orders.length < 1 ) return;
        Root root = (Root) query.getRoots().iterator().next();
        List<Order> lista = new ArrayList<>(6);
        for( String s : orders ){ 
            try{
                String[] ordersOrder = s.trim().split("\\s+");
                if( ordersOrder.length > 1 && ordersOrder[1].matches("(?i)desc") ) {
                    lista.add( cb.desc(root.get( ordersOrder[0] ) ) );
                }else lista.add( cb.asc( root.get( ordersOrder[0] ) ) );
            }catch(IllegalArgumentException ex){
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
        query.orderBy( lista );
    }
    
}
