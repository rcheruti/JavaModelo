
package br.eng.rcc.framework.jaxrs.persistence;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.utils.EntidadesUtils;
import br.eng.rcc.seguranca.servicos.SegurancaServico;
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
@Path("/persistence")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
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
    @Inject
    private EntidadesUtils emUtils;
    
    private final Pattern queryStringPattern = Pattern
        .compile("([\\w.]++)\\s*+(=|!=|<|>|<=|>=|(?>not)?like)\\s*+((['\"]).*?\\4|[\\w\\.]++)\\s*+([&\\|]?)");
    
    /**
     * Para que este objeto possa fazer o seu trabalho, é obrigatório um 
     * {@link javax.persistence.EntityManager EntityManager} para acessar o banco.
     */
    @PostConstruct
    public void postConstruct(){
        if( em == null ){
            String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
            System.out.println(msg);
            throw new MsgException(msg);
        }
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
    public Object getEntidade(
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
            joinParams = joinMatrix.split("[,\\s]+");
        }else{
            joinParams = new String[0];
        }
        
        String[] orderParams;
        if( orderMatrix != null ){
            orderParams = orderMatrix.split("[,\\s]+");
        }else{
            orderParams = new String[0];
        }
        
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            //Logger.getLogger(this.getClass().getName()).log(
            //        Level.SEVERE, "A classe do nome ''{0}'' não foi encontrada.", entidade);
            return null;
        }
        checker.check( klass, checker.SELECT );
        
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
        emUtils.nullifyLazy( em, res.toArray(), joinParams );

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
     * @param obj
     * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
     */
    @POST @Path("/{entidade}")
    @Transactional
    public Object createEntidade(Object obj){
            System.out.println("--->>>  Param obj: "+ obj.getClass() );
        if( obj == null ){
            //Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, 
            //    "O Parâmetro para 'EntidadesService.createEntidade' não pode ser nulo.");
            return new JsonResponse(false,
                "O Parâmetro para 'EntidadesService.createEntidade' não pode ser nulo.");
        }
        checker.check( obj.getClass(), checker.INSERT );
        //em.persist(obj);
        return new JsonResponse(true,"Objeto gravado com sucesso");
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
    public Object editEntidade(
            @PathParam("entidade") String entidade,
            @Context UriInfo ctx,
            JsonNode obj){ // JsonNode
        if( obj == null ){
            return new JsonResponse(false,
                "O Parâmetro para 'EntidadesService.editEntidade' não pode ser nulo.");
        }
        String uriQuery = ctx.getRequestUri().getQuery();
        if( uriQuery == null || uriQuery.isEmpty() ){
            return new JsonResponse(false,
                "Para editar registros é necessário informar os parâmetros de filtragem na QueryString.");
        }
        String[][] querysPs = parseQueryString(uriQuery);
        
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            Logger.getLogger(this.getClass().getName()).log(
                    Level.WARNING, "A classe do nome ''{0}'' não foi encontrada.", entidade);
            return new JsonResponse(false,
                "A classe não foi encontrada");
        }
        checker.check( klass, checker.UPDATE);
        
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
            if( emUtils.constainsInArray(querysPsNames, nodeName) ) continue;
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
        
        return new JsonResponse(true,ups, "Objetos atualizados com sucesso");
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
    public Object deleteEntidade(
                @PathParam("entidade") String entidade ,
                @Context UriInfo ctx
            ){
        
        String uriQuery = ctx.getRequestUri().getQuery();
        if( uriQuery == null || uriQuery.isEmpty() ){
            return new JsonResponse(false,
                "Para apagar registros é necessário informar os parâmetros de filtragem na QueryString.");
        }
        String[][] querysPs = parseQueryString(uriQuery);
        
        
        Class<?> klass = cache.get(entidade, em);
        if( klass == null ){
            Logger.getLogger(this.getClass().getName()).log(
                    Level.WARNING, "A classe do nome ''{0}'' não foi encontrada.", entidade);
            return new JsonResponse(false,
                "A classe não foi encontrada");
        }
        checker.check( klass, checker.DELETE);
        
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
        
        return new JsonResponse(true,qtd,"Objeto apagado com sucesso");
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
                String[] params = { attr, comp , valor ,opComp };
                //querysPs.add(params);
                querysPs[qI++] = params;
            }
        }
        String[][] resQueryPs = new String[qI][];
        System.arraycopy(querysPs, 0, resQueryPs, 0, qI);
        return resQueryPs;
    }
    
    private void addOrderBy(CriteriaBuilder cb, CriteriaQuery query, String[] orders){
        Root root = (Root) query.getRoots().iterator().next();
        List<Order> lista = new ArrayList<>(6);
        for( String s : orders ){
            lista.add( cb.asc( root.get(s) ) );
        }
        query.orderBy( lista );
    }
    
}
