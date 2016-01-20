
package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.utils.PersistenceUtils;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.ws.rs.core.MediaType;

/**
 * 
 * 
 * @author rcheruti
 */
@Path("/persistencia")
@Produces({ Configuracoes.JSON_PERCISTENCIA })
@Consumes({ Configuracoes.JSON_PERCISTENCIA })
@RequestScoped
public class EntidadesService {
    private final static Map<String,Runnable> map = new HashMap<>();
    
    private final int pageNumDefault = 0;
    private final int pageSizeDefault = 20;
    
    @Inject
    private EntityManager em;
    @Inject
    private ClassCache cache;
    @Inject
    private SegurancaServico checker;
    //@Inject
    //private PersistenceUtils emUtils;
    
    private final Pattern queryStringPattern = Pattern
        .compile("([\\w.]++)\\s*+(=|!=|<|>|<=|>=|(?>not)?like)\\s*+((['\"]).*?\\4|[\\w\\.]++)\\s*+([&\\|]?)", Pattern.CASE_INSENSITIVE);
    private final Pattern valorPattern = Pattern
        .compile("^(['\"]).*\\1$", Pattern.CASE_INSENSITIVE);
    
    
    /**
     * Para que este objeto possa fazer o seu trabalho, é obrigatório um 
     * {@link javax.persistence.EntityManager EntityManager} para acessar o banco.
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
    
    /** 
     * Exemplo de URL para abusca:
     * "http://www.endereco.com.br/jaxRootContext/persistence/MinhaClasse;matrix1=valor;matrix2=valor?attr1=valor & attr2 != valor2"
     * 
     * Ex.:
     * ".../persistence/Comprador;join=carros;order=nome;page=3;size=30? idade > 18"
     * Retornará as entidades da classe "Comprador" que tenham o atributo "idade" (a classe DEVE ter este atributo) maior que 18,
     * ordenados pelo "nome", sendo que a busca retornará a página 4 (pois "page" inicia em 0) limitada em 30 itens.
     * 
     * @param entidade Nome simples da classe que iremos buscar uma entidade.
     * @param ctx Este parâmetro será informado pelo JAX-RS, daqui pegaremos a "Query String"
     * que será usada para fazer a filtragem das entidades (cláusula WHERE).
     * @param pageNumStr Este parâmetro informa a página que deverá ser retornada (cláusula OFFSET), iniciando em 0.
     * @param pageSizeStr Este parâmetro informa a quantidade de itens que retornarão por página (cláusula LIMIT).
     * @param joinMatrix Este parâmetro informa quais atributos devemos incluir no "FETCH JOIN" (cláusulas JOIN).
     * @param orderMatrix Este parâmetro informa quais atributos serão usados para a ordenação (cláusula ORDER BY).
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @GET @Path("/{entidade}")
    public Object buscarEntidade(
                // Nome da classe (entidade) que iremos usar como parâmetro principal
            @PathParam("entidade") String entidade ,
                // Apenas para coletarmos dados da requisição, não vem da URL
            @Context UriInfo ctx,
                // 'Pagina' da busca, cláusula OFFSET do banco
            @MatrixParam("page") String pageNumStr,
                // Quantidade de itens na página, cláusula LIMIT do banco
            @MatrixParam("size") String pageSizeStr,
                // Itens relacionados que devem ser carregados, cláusula JOIN FETCH da JPQL
            @MatrixParam("join") String joinMatrix,
            
            @MatrixParam("order") String orderMatrix
        ){
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
        }
        checker.check( klass, Seguranca.SELECT );
        
        
        
        
        int pageNum, pageSize;
        try{
            pageNum = Integer.parseInt(pageNumStr);
        }catch(Exception ex){
            pageNum = pageNumDefault;
        }
        try{
            pageSize = Integer.parseInt(pageSizeStr);
        }catch(Exception ex){
            pageSize = pageSizeDefault;
        }
        
        String uriQuery = ctx.getRequestUri().getQuery();
        String[][] querysPs = parseQueryString(uriQuery);
        
        // ----  Interpretando os parâmetros passados: 
        MultivaluedMap<String,String> querysParams = ctx.getQueryParameters();
        //List<String> joinParams = new ArrayList<>();
        String[] joinParams;
        if( joinMatrix != null ){
            joinParams = joinMatrix.split(",+");
        }else{
            joinParams = new String[0];
        }
        
        String[] orderParams;
        if( orderMatrix != null ){
            orderParams = orderMatrix.split(",+");
        }else{
            orderParams = new String[0];
        }
        
        
        // ----  Criando a busca ao banco:
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery query = cb.createQuery();
        Root root = query.from( klass );
        query.select(root);
        
        
        // Cláusula JOIN FETCH da JPQL:
        for(String s : joinParams){
            root.fetch(s, JoinType.LEFT);
        }
        
        // Cláusula ORDER BY da JPQL:
        addOrderBy( cb, query, orderParams );
        
        
        // Cláusula WHERE do banco:
        WhereBuilderInterface wb = WhereBuilder.create(cb, query);
        query.where( wb.addArray( querysPs ).build() );
        
        // A busca ao banco:
        Query q = em.createQuery(query);
        q.setFirstResult( pageNum*pageSize );
        q.setMaxResults( pageSize );
        Set<Object> res = new LinkedHashSet<>( q.getResultList() );
        PersistenceUtils.nullifyLazy( em, res.toArray(), joinParams );

        // A resposta: 
        return new JsonResponse(true,res,"Lista dos objetos", pageNum, pageSize);
        
    }
    
    
    /**
     * Este método deve receber no "PathParam" o nome simples da classe que iremos criar
     * uma nova entidade no banco de dados.
     * 
     * Os dados dessa entidade devem ser enviados no corpo do POST, no formato JSON.
     * Atributos que não estejam presentes receberão o seu valor padrão (valor padrão da instrução "new"),
     * e atributos que não pertençam a esta entidade serão ignorados.
     * 
     * Apenas uma entidade de cada vez é permitida para este método.
     * 
     * @param objs Lista de objetos para persistência no banco de dados
     * @param entidade Nome da entidade de banco de dados
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @POST @Path("/{entidade}")
    @Transactional
    public Object criarEntidade(List<?> objs, @PathParam("entidade") String entidade){
        if( objs == null || objs.isEmpty() ){
            return new JsonResponse(false,
                String.format("Não encontramos nenhuma entidade para '%s'", entidade) );
        }
        checker.check( objs.get(0).getClass(), Seguranca.INSERT );
        for( Object obj : objs ) em.persist(obj);
        return new JsonResponse(true,null);
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
    @PUT @Path("/{entidade}")
    @Transactional
    public Object editarEntidade(
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
        String[][] querysPs = parseQueryString(uriQuery);
        
        
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
            if( PersistenceUtils.constainsInArray(querysPsNames, nodeName) ) continue;
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
    @DELETE @Path("/{entidade}")
    @Transactional
    public Object deletarEntidade(
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
        String[][] querysPs = parseQueryString(uriQuery);
        
        
        
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
        
        
        // A busca ao banco:
        int qtd = em.createQuery(query).executeUpdate();
        
        return new JsonResponse(true,qtd,null);
    }
    
    
    //=============================================================================
    //=============================================================================
    //=============================================================================
    
    /*
        Abaixo estão os métodos que trabalharão com lotes de entidades,
        esses métodos têm que saber lidar com listas que terão vários tipo de entidades
        diferentes misturadas
    */
    
    @GET @Path("/many")
    @Transactional
    public Object buscarVariasEntidades(List<?> objs){
        
        return new JsonResponse(true, null);
    }
    
    @POST @Path("/many")
    @Transactional
    public Object criarVariasEntidades(List<?> objs){
        
        return new JsonResponse(true, null);
    }
    
    @PUT @Path("/many")
    @Transactional
    public Object editarVariasEntidades(List<?> objs){
        
        return new JsonResponse(true, null);
    }
    
    @DELETE @Path("/many")
    @Transactional
    public Object deletarVariasEntidades(List<?> objs){
        
        return new JsonResponse(true, null);
    }
    
    
    //===============  Privates  ==================
    /**
     * Este método é usado internamente para criar os itens de filtragem das buscas.
     * 
     * @param uriQuery
     * @return 
     */
    public String[][] parseQueryString(String uriQuery){
        // Interpretando "Query String" (parâmetros de busca no banco [WHERE])
        //List<String[]> querysPs = new ArrayList<>();
        int qI = 0, qLimit = 30;
        String[][] querysPs = new String[qLimit][];
        if( uriQuery != null ){
            Matcher m = queryStringPattern.matcher(uriQuery);
            while( m.find() ){
                String attr = m.group(1);
                String comp = m.group(2);
                String valor = m.group(3);
                String opComp = m.group(5);
                if( attr == null || comp == null || valor == null ) continue;
                
                boolean isValor = valorPattern.matcher(valor).find();
                if( isValor ) valor = valor.substring(1, valor.length()-1 );
                
                String[] params = { attr, comp , valor , opComp, isValor?"":null };
                //querysPs.add(params);
                querysPs[qI++] = params;
            }
        }
        String[][] resQueryPs = new String[qI][];
        System.arraycopy(querysPs, 0, resQueryPs, 0, qI);
        return resQueryPs;
    }
    
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
