package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.persistencia.builders.WhereBuilder;
import br.eng.rcc.framework.jaxrs.persistencia.builders.WhereBuilderInterface;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
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

  public List<Object> buscar(Class<?> klass, PersistenciaUtils.BuscaInfo info) {
    
    checker.check( klass, Seguranca.SELECT );
    
    // ----  Criando a busca ao banco:
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery query = cb.createQuery();
    Root root = query.from( klass );
    query.select(root);

    // Cláusula JOIN FETCH da JPQL:
    for(String s : info.join ){ 
      if( s.indexOf('.') < 0 )
        root.fetch(s, JoinType.LEFT);
    }

    // Cláusula ORDER BY da JPQL:
    addOrderBy( cb, query, info.order );


    // Cláusula WHERE do banco:
    WhereBuilderInterface wb = WhereBuilder.create(cb, query);
    query.where( wb.addArray( info.query ).build() );

    checker.checkPersistencia(klass, cb, query);

    // A busca ao banco:
    Query q = em.createQuery(query);
    q.setFirstResult( info.page * info.size );
    q.setMaxResults( info.size );
    List<Object> res = q.getResultList();
    PersistenciaUtils.resolverLazy(cache, res.toArray(), false, info.join );
    this.em.clear();
    PersistenciaUtils.resolverLazy(cache, res.toArray(), true, info.join );

    return res;
  }

  public void criar() {

  }

  public void editar() {

  }

  public void deletar() {

  }
  
  public void adicionar(){
    
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
  
}
