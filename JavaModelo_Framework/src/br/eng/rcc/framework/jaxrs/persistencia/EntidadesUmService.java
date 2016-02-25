
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
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


@Path("/persistencia/{entidade}")
@Produces({ Configuracoes.JSON_PERSISTENCIA })
@Consumes({ Configuracoes.JSON_PERSISTENCIA })
@RequestScoped
public class EntidadesUmService {
    private final static Map<String,Runnable> map = new HashMap<>();
    
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
      checker.check( klass, Seguranca.SELECT | Seguranca.INSERT 
                          | Seguranca.DELETE | Seguranca.UPDATE );
      
      Metamodel meta = this.em.getMetamodel();
      EntityType entity = meta.entity(klass);
      
      Map<String,String> map = new HashMap<>(20);
      Set<Attribute> attrs = entity.getDeclaredAttributes();
      for( Attribute attr : attrs ){
        if( attr.isCollection() ){
          PluralAttribute pAttr = (PluralAttribute) attr;
          map.put( pAttr.getName(), String.format("%s<%s>",
                  pAttr.getJavaType().getSimpleName(), 
                  pAttr.getElementType().getJavaType().getSimpleName()  ) );
        }else{
          map.put( attr.getName(), attr.getJavaType().getSimpleName() );
        }
      }
      
      return new JsonResponse(true, map, "Busca do tipo");
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
    @GET @Path("/")
    @Transactional
    public JsonResponse buscar(
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
        if( pageSize > Configuracoes.limiteEntidadesSize ) pageSize = Configuracoes.limiteEntidadesSize;
        
        String uriQuery = ctx.getRequestUri().getQuery();
        String[][] querysPs = PersistenciaUtils.parseQueryString(uriQuery);
        
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
        
        checker.checkPersistencia(klass, cb, query);
        
        // A busca ao banco:
        Query q = em.createQuery(query);
        q.setFirstResult( pageNum*pageSize );
        q.setMaxResults( pageSize );
        Set<Object> res = new HashSet<>( q.getResultList() );
        this.em.clear();
        PersistenciaUtils.anularLazy(cache, res.toArray(), joinParams );

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
        
        
        //-------------------------------------------------
        /*
        Class<?> klass = cache.get(entidade);
        Metamodel mm = em.getMetamodel();
        EntityType et = mm.entity(klass);
        Set<PluralAttribute> plurals = et.getDeclaredPluralAttributes();
        
        //List<Method> cascadeCalls = new ArrayList<>();
        Map<Method,List<Method>> mapCascade = new HashMap<>();
        System.out.printf("Iniciando: plurals: %d \n", plurals.size() ).flush();
        for( PluralAttribute pAttr : plurals ){
          System.out.printf("-------------------- \n" ).flush();
          System.out.printf("PluralAttribute: %s \n", pAttr.getName() ).flush();
          if( !pAttr.isAssociation() ) continue; // ----------  não processar esse
          
          Field field = null;
          try{
            field = pAttr.getDeclaringType().getJavaType().getDeclaredField( pAttr.getName() );
          }catch( NoSuchFieldException ex ){
            System.out.printf("Não existe esse campo na classe: %s\n", pAttr.getName()).flush();
            continue;
          }
          String attrName = null;
          
          // ====  pegar nome de atributo e se devemos propagar a chamada (cascade):
          boolean cascade = false;
          CascadeType[] cascadeArr = null;
          ManyToMany mTmAnn = (ManyToMany)field.getAnnotation( ManyToMany.class );
          if( mTmAnn != null ){
            cascadeArr = mTmAnn.cascade();
            attrName = mTmAnn.mappedBy();
          }else{
            OneToMany oTmAnn = (OneToMany)field.getAnnotation( OneToMany.class );
            if( oTmAnn != null ){
              cascadeArr = oTmAnn.cascade();
              attrName = oTmAnn.mappedBy();
            }
          }
          System.out.printf("Classe: %s, Campo: %s, attrName: %s, cascadeArr: %s \n", 
                  klass.getName(), field.getName() ,attrName, cascadeArr ).flush();
          if( attrName == null || attrName.isEmpty() || cascadeArr == null ) continue;
          for( CascadeType cT : cascadeArr ){
            if( cT.equals( CascadeType.ALL ) || cT.equals( CascadeType.PERSIST ) ){
              cascade = true;
              break;
            }
          }
          System.out.printf("cascade: %b \n", cascade).flush();
          if( !cascade ) continue; // ----------  não processar esse
              //  --- 
          
          Method mGet = null;
          String mName = null;
          try{
            mName = pAttr.getName().replaceFirst(pAttr.getName().substring(0,1), 
                                pAttr.getName().substring(0,1).toUpperCase() );
            mGet = pAttr.getDeclaringType().getJavaType().getMethod("get"+ mName);
            List<Method> methods = new ArrayList<>();
            mapCascade.put( mGet, methods );
            
            Method mmm = null;
            Class<?> typeClass = pAttr.getElementType().getJavaType();
            EntityType refEt = mm.entity( typeClass );
            
            System.out.printf("attrName: %s\n", attrName ).flush();
            Attribute refAttr = refEt.getDeclaredAttribute(attrName);
            
            System.out.printf("refAttr: %s\n", refEt.getName() ).flush();
            if( refAttr == null ) continue; // ----------  não processar esse
            
            if( refAttr.isCollection() ){
              System.out.printf("refAttr: Plural \n" ).flush();
              mName = refAttr.getName().replaceFirst(refAttr.getName().substring(0,1), 
                                refAttr.getName().substring(0,1).toUpperCase() );
              mmm = typeClass.getMethod("get"+ mName);
              methods.add(mmm);
              
              mmm = Collection.class.getMethod("add", Object.class );
              methods.add(mmm);
            }else{
              System.out.printf("refAttr: Singular \n" ).flush();
              mName = "set"+ refAttr.getName().replaceFirst(refAttr.getName().substring(0,1), 
                                refAttr.getName().substring(0,1).toUpperCase() );
              System.out.printf("typeClass: %s, mName: %s \n", typeClass.getName(), mName ).flush();
              mmm = typeClass.getMethod( mName, klass);
              methods.add(mmm);
            }
            
            
            //Method mSet = pAttr.getJavaType().getMethod("set"+ mName, pAttr.getJavaType());
            //mapCascade.put(mGet,mSet);
          }catch( NoSuchMethodException ex ){
            System.out.printf("removendo alguem!!  NoSuchMethodException: %s \n",mName ).flush();
            if( mGet != null ) mapCascade.remove(mGet);
          }
        }
        
        System.out.printf("mapCascade.size: %d\n", mapCascade.size() ).flush();
        
        for( Object obj : objs ){
          
          for( Method get : mapCascade.keySet() ){
            try{
              System.out.printf("Chamar '%s' em '%s' \n", get, obj ).flush();
              Collection lista = (Collection)get.invoke(obj);
              if( lista == null || lista.isEmpty() ) continue;
              List<Method> sets = mapCascade.get(get);
              Method set;
              int i=0; 
              System.out.printf("sets.size: %d \n", sets.size() ).flush();
              for( Object alvo : lista ){
                if( sets.size() > 1 ) for( ; i < sets.size()-1; i++ ){
                  set = sets.get(i);
                  alvo = set.invoke( alvo );
                }
                set = sets.get(i);
                System.out.printf("Chamar '%s' em '%s' com '%s' \n", set, alvo, obj ).flush();
                set.invoke( alvo , obj);
              }
            }catch( IllegalAccessException | InvocationTargetException ex ){
              System.out.printf("Esse sistema não tem permissão para fazer chamadas Reflectivas em objetos.").flush();
            }catch( NullPointerException ex ){
              System.out.printf("NullPointerException para: %s \n", get.getName() ).flush();
            }
          }
        }
          /* */
          
        
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
