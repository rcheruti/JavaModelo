
package br.eng.rcc.framework.jaxrs.persistencia;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

@ApplicationScoped
public class ClassCache {
    
    @Inject
    private EntityManager emInj;
    
    private volatile SoftReference< Map<String,Class<?>> > ref;
    private volatile SoftReference< Map<String,Function> > refEstrategias;
    
    //===================================================================
    
    public Class<?> get(String entidadeName){
        return get(entidadeName, null);
    }
    public Class<?> get(String entidadeName, EntityManager em){
        if( em == null ) em = emInj ;
        if( ref == null ){
            if( em == null ) return null;
            reloadCache(em);
        }
        Map<String,Class<?>> map = ref.get();
        if( map == null ){
            if( em == null ) return null;
            reloadCache(em);
            map = ref.get();
        }
        return map.get(entidadeName);
    }
    
    public Function getEstrategiaInsert(String entidadeName){
      Class<?> klass = get(entidadeName);
      if( klass == null ) return null ;
      
      List< Class<?> > relacoes = new ArrayList<>();
      
      Metamodel mm = emInj.getMetamodel();
      EntityType et = mm.entity(klass);
      
      et.getDeclaredSingularAttributes();
      
      
      return null;
    }
    public Function getEstrategiaUpdate(String entidadeName){
      
      return null;
    }
    
    //===================================================================
    
    private synchronized void reloadCache(EntityManager em){
        if( em == null || ref != null && ref.get() != null ) return ;
        Metamodel metamodel = em.getMetamodel();
        Map<String,Class<?>> map = new HashMap<>( metamodel.getEntities().size() + 2 );
        for( EntityType<?> entity : metamodel.getEntities() ){
            //System.out.printf("---  Carregando do Metamodel: %s [%s] \n", entity.getName(), entity.getJavaType() );
            map.put( entity.getName(), entity.getJavaType() );
        }
        ref = new SoftReference( map );
    }
    
}
