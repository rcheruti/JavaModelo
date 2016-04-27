package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.jaxrs.MsgException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Embedded;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

@ApplicationScoped
public class ClassCache {

  @Inject
  private EntityManager emInj;

  private SoftReference< Map<String, Class<?>>> ref;
  private SoftReference< Map<String, Map<String, ClasseAtributoUtil>>> beanInfos;

  //===================================================================
  public Class<?> get(String entidadeName) {
    return get(entidadeName, null, false);
  }
  public Class<?> get(String entidadeName, EntityManager em) {
    return get(entidadeName, em, false);
  }
  public Class<?> get(String entidadeName, EntityManager em, boolean retornar) {
    if (em == null) {
      em = emInj;
    }
    if (ref == null) {
      if (em == null) {
        return null;
      }
      reloadCache(em);
    }
    Map<String, Class<?>> map = ref.get();
    if (map == null) {
      if (em == null) {
        return null;
      }
      reloadCache(em);
      map = ref.get();
    }
    Class<?> x = map.get(entidadeName);
    if (x == null && !retornar) {
      throw new MsgException(String.format("Não encontramos nenhuma entidade para '%s'", entidadeName));
    }
    return x;
  }
  
  public Map<String, ClasseAtributoUtil> getInfo(String entidadeName) {
    return getInfo(entidadeName, emInj);
  }
  public Map<String, ClasseAtributoUtil> getInfo(String entidadeName, EntityManager em) {
    recarregarBeanInfo(em);
    return beanInfos.get().get(entidadeName);
  }
  public Map<String, ClasseAtributoUtil> getInfo(Class<?> classe) {
    return getInfo("", emInj);
  }
  public Map<String, ClasseAtributoUtil> getInfo(Class<?> classe, EntityManager em) {
    recarregarBeanInfo(em);
    return beanInfos.get().get(null);
  }

  private void recarregarBeanInfo(EntityManager em) {
    if (em == null || beanInfos != null && beanInfos.get() != null)return;

    synchronized (this) {
      if ( beanInfos != null && beanInfos.get() != null)return;
      
      reloadCache(em);
      try {
        Map<String, Class<?>> map = ref.get();
        Metamodel meta = em.getMetamodel();
        Map<String, Map<String, ClasseAtributoUtil>> mapInfo = new HashMap<>(60);
        for (String nome : map.keySet()) {
          // primeiro pegar os nomes dos atributos que precisamos:
          Map<String, ClasseAtributoUtil> mapInfoProps = mapInfo.get(nome);
          // pode ainda não existir esse mapa:
          if (mapInfoProps == null) {
            mapInfoProps = new HashMap<>(12);
            mapInfo.put(nome, mapInfoProps);
          }
          Class<?> klass = map.get(nome);
          ManagedType type = meta.managedType(klass);
          BeanInfo info = Introspector.getBeanInfo(klass);
          PropertyDescriptor[] propDescs = info.getPropertyDescriptors();
          if (type.getAttributes() != null && propDescs != null) {
            for (Attribute attr : (Set<Attribute>) type.getAttributes()) {

              for (PropertyDescriptor propDesc : propDescs) {
                if (!attr.getName().equals(propDesc.getName())) {
                  continue;
                }
                ClasseAtributoUtil beanUtil = new ClasseAtributoUtil();
                beanUtil.classCache = this;
                beanUtil.nome = attr.getName();
                beanUtil.getter = propDesc.getReadMethod();
                beanUtil.setter = propDesc.getWriteMethod();
                beanUtil.associacao = attr.isAssociation();
                if (attr instanceof PluralAttribute) {
                  beanUtil.colecaoType = attr.getJavaType();
                  beanUtil.javaType = ((PluralAttribute) attr).getBindableJavaType();
                } else {
                  beanUtil.javaType = attr.getJavaType();
                }
                if (attr.isCollection()) {
                  beanUtil.colecao = attr.isCollection();
                  Class<?> cColl = attr.getJavaType();
                  if (Collection.class.isAssignableFrom(cColl)) {
                    beanUtil.add = cColl.getMethod("add", Object.class);
                    beanUtil.remove = cColl.getMethod("remove", Object.class);
                  }
                }
                // pegando anotações:
                Field field = ((Field) attr.getJavaMember());
                if (attr.isAssociation()) {
                  OneToMany annOTM = field.getAnnotation(OneToMany.class);
                  if (annOTM != null) {
                    beanUtil.mapeado = annOTM.mappedBy();
                  } else {
                    OneToOne annOTO = field.getAnnotation(OneToOne.class);
                    if (annOTO != null) {
                      beanUtil.mapeado = annOTO.mappedBy();
                    }
                  }
                } else {
                  // verificando os embutidos:
                  Embedded annEMBD = field.getAnnotation(Embedded.class);
                  if (annEMBD != null) {
                    beanUtil.embutido = true;
                  }
                }
                mapInfoProps.put(propDesc.getName(), beanUtil);
              }
            }
          }
        }
        beanInfos = new SoftReference(mapInfo);
      } catch (IntrospectionException | NoSuchMethodException ex) {
        throw new RuntimeException("Exceção de Instrospecção", ex);
      }
    }

  }

  //===================================================================
  private void reloadCache(EntityManager em) {
    if (em == null || ref != null && ref.get() != null)return;

    synchronized (this) {
      if (ref != null && ref.get() != null) return;
      
      Metamodel metamodel = em.getMetamodel();
      Map<String, Class<?>> map = new HashMap<>(metamodel.getManagedTypes().size() + 2);
      for (ManagedType<?> entity : metamodel.getManagedTypes()) {
        //System.out.printf("---  Carregando do Metamodel: %s [%s] \n", entity.getName(), entity.getJavaType() );
        map.put(entity.getJavaType().getSimpleName(), entity.getJavaType());
      }
      ref = new SoftReference(map);
    }

  }

  //===================================================================
}
