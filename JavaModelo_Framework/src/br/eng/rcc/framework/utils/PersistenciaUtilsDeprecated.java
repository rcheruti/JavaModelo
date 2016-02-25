
package br.eng.rcc.framework.utils;

import static br.eng.rcc.framework.utils.PersistenciaUtils.copyWithoutNulls;
import static br.eng.rcc.framework.utils.PersistenciaUtils.nullifyInArray;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

public class PersistenciaUtilsDeprecated {
  
  private static final Pattern queryStringPattern = Pattern
       .compile("([\\w.]++)\\s*+(=|!=|<|>|<=|>=|(?>not)?like)\\s*+((['\"]).*?\\4|[\\w\\.]++)\\s*+([&\\|]?)", Pattern.CASE_INSENSITIVE);
  private static final Pattern valorPattern = Pattern
       .compile("^(['\"]).*\\1$", Pattern.CASE_INSENSITIVE);
  
  
  
  @Deprecated
  public static void nullifyLazy(EntityManager em, Object obj,
          String[] params){
    Object[] lista = { obj };
    nullifyLazy(em, lista);
  }
  @Deprecated
  public static void nullifyLazy(EntityManager em, Object[] lista,
          String... params) {
    nullifyLazy(em, lista, 0, params);
  }
  @Deprecated
  public static void nullifyLazy(EntityManager em, Object[] lista,
          int secureLevel, String... params ) {
    // Se a árvore descer demais, podemos estar em recursão infinita,
    // temos que evitar isso aconteça
    if (lista == null) {
      return;
    }
    if (secureLevel++ >= 30) {
      throw new IllegalArgumentException("Um dos parâmetros esta fazendo com que entremos em recursão infinita!");
    }
    if (lista.length > 0) {
      
      List<Field> fieldsToNullify = new ArrayList<>();
      List<Field> fieldsDownNullify = new ArrayList<>();
      
      Metamodel meta = em.getMetamodel();
      EntityType type = meta.entity(lista[0].getClass());
      Set<Attribute> attrs = type.getDeclaredAttributes();
      for (Attribute attr : attrs) {
        if (!attr.isAssociation()) {
          continue;
        }
        String name = attr.getName();
        Field member = (Field) attr.getJavaMember();
        if (!nullifyInArray(params, name)) {
          fieldsToNullify.add(member);
        } else {
          // Descer a arvore para nullify
          fieldsDownNullify.add(member);
        }
      }
      params = copyWithoutNulls(params);

      // Aplicar valor NULL nas lista que são LAZY:
      for (Field field : fieldsToNullify) {
        for (Object obj : lista) {
          if( obj == null ) continue;
          try {
            field.set(obj, null);
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(PersistenciaUtils.class.getName()).log(Level.WARNING, null, ex);
            return;
          }
        }
      }
      // Descer a arvore para nullify:
      for (Field field : fieldsDownNullify) {
        for (Object obj : lista) {
          if( obj == null ) continue;
          try {
            Object campoValor = field.get(obj) ;
            if( campoValor != null ){
              if( campoValor instanceof Collection ){
                nullifyLazy(em, ((Collection)campoValor).toArray(),
                      secureLevel, params );
              }else{
                Object[] campoValorArr = { campoValor };
                nullifyLazy(em, campoValorArr,
                      secureLevel, params );
              } 
            }
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(PersistenciaUtils.class.getName()).log(Level.WARNING, null, ex);
            return;
          }
        }
      }
    }
  }
  
  
  
  
}
