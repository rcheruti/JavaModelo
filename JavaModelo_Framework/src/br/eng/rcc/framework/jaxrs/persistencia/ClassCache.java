
package br.eng.rcc.framework.jaxrs.persistencia;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

@ApplicationScoped
public class ClassCache {
    
    @Inject
    private EntityManager emInj;
    
    private volatile SoftReference< Map<String,Class<?>> > ref;
    private volatile SoftReference< Map<String, Map<String,BeanUtil> > > beanInfos;
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
    
    
    public Map<String,BeanUtil> getInfo(String entidadeName){
      return getInfo(entidadeName, null);
    }
    public Map<String,BeanUtil> getInfo(String entidadeName, EntityManager em){
      recarregarBeanInfo(em);
      return beanInfos.get().get(entidadeName);
    }
    public synchronized void recarregarBeanInfo(EntityManager em){
      if( em == null || beanInfos != null && beanInfos.get() != null ) return ;
      reloadCache(em);
      try{
        Map<String,Class<?>> map = ref.get();
        Metamodel meta = em.getMetamodel();
        Map<String, Map<String,BeanUtil> > mapInfo = new HashMap<>(60);
        for( String nome : map.keySet() ){
            // primeiro pegar os nomes dos atributos que precisamos:
          Map<String,BeanUtil> mapInfoProps = mapInfo.get(nome);
          if( mapInfoProps == null ){
            mapInfoProps = new HashMap<>(12);
            mapInfo.put(nome, mapInfoProps);
          }
          Class<?> klass = map.get(nome);
          EntityType type = meta.entity(klass);
          BeanInfo info = Introspector.getBeanInfo( klass );
          PropertyDescriptor[] propDescs = info.getPropertyDescriptors();
          if( type.getAttributes() != null && propDescs != null ){
            for( Attribute attr : (Set<Attribute>)type.getAttributes() ){
              
              for( PropertyDescriptor propDesc : propDescs ){
                if( !attr.getName().equals( propDesc.getName() ) ) continue;
                BeanUtil beanUtil = new BeanUtil();
                beanUtil.getter = propDesc.getReadMethod();
                beanUtil.setter = propDesc.getWriteMethod();
                beanUtil.associacao = attr.isAssociation();
                if( attr.isCollection() ){
                  Class<?> cColl = attr.getJavaType();
                  if( Collection.class.isAssignableFrom(cColl) ){
                    beanUtil.add = cColl.getMethod("add", Object.class);
                    beanUtil.remove = cColl.getMethod("remove", Object.class);
                  }
                }
                mapInfoProps.put(propDesc.getName(), beanUtil);
              }
            }
          }
        }
        beanInfos = new SoftReference( mapInfo );
      }catch(IntrospectionException | NoSuchMethodException ex){
        beanInfos = null;
      }
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
    
    //===================================================================
    
    public static class BeanUtil {
      
      private boolean associacao;
      private Method getter;
      private Method setter;
      private Method add;
      private Method remove;
      
      
      public boolean isAssociacao(){ return associacao; }
      public Method getGetter(){ return getter; }
      public Method getSetter(){ return setter; }
      public Method getAdd(){ return add; }
      public Method getRemove(){ return remove; }
      
      public Object get(Object that) 
              throws IllegalAccessException, InvocationTargetException{ 
        if( getter == null ) return null;
        return getter.invoke(that);
      }
      public void set(Object that, Object param)
              throws IllegalAccessException, InvocationTargetException{
        if( setter != null ) setter.invoke(that, param);
      }
      public void add(Object that, Object... params)
              throws IllegalAccessException, InvocationTargetException{
        if( setter != null ) 
          for( Object param : params )
            setter.invoke(that, param);
      }
      public void remove(Object that, Object... params)
              throws IllegalAccessException, InvocationTargetException{
        if( setter != null ) 
          for( Object param : params )
            setter.invoke(that, param);
      }
      
    }
    
}
