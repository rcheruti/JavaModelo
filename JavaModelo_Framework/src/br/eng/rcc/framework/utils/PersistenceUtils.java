package br.eng.rcc.framework.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

public class PersistenceUtils {
  
  private static final Pattern queryStringPattern = Pattern
       .compile("([\\w.]++)\\s*+(=|!=|<|>|<=|>=|(?>not)?like)\\s*+((['\"]).*?\\4|[\\w\\.]++)\\s*+([&\\|]?)", Pattern.CASE_INSENSITIVE);
  private static final Pattern valorPattern = Pattern
       .compile("^(['\"]).*\\1$", Pattern.CASE_INSENSITIVE);

  
  
  
  public static void nullifyLazy(EntityManager em, Object[] lista,
          String[] params) {
    nullifyLazy(em, lista, params, 0);
  }

  public static void nullifyLazy(EntityManager em, Object[] lista,
          String[] params, int secureLevel) {
    // Se a árvore descer demais, podemos estar em recursão infinita,
    // temos que evitar isso aconteça
    if (lista == null) {
      return;
    }
    if (secureLevel++ >= 50) {
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
            Logger.getLogger(PersistenceUtils.class.getName()).log(Level.WARNING, null, ex);
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
                      params, secureLevel);
              }else{
                Object[] campoValorArr = { campoValor };
                nullifyLazy(em, campoValorArr,
                      params, secureLevel);
              } 
            }
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(PersistenceUtils.class.getName()).log(Level.WARNING, null, ex);
            return;
          }
        }
      }
    }
  }

  public static boolean constainsInArray(Object[] arr, Object obj) {
    if (arr == null || obj == null) {
      return false;
    }
    int hash = obj.hashCode();
    for (int i = 0; i < arr.length; i ++) {
      Object x = arr[i];
      if( x == null )continue;
      if (hash == x.hashCode() && obj.equals(x)) {
        return true;
      }
    }
    return false;
  }
  public static boolean nullifyInArray(Object[] arr, Object obj) {
    if (arr == null || obj == null) {
      return false;
    }
    int hash = obj.hashCode();
    for (int i = 0; i < arr.length; i ++) {
      Object x = arr[i];
      if( x == null )continue;
      if (hash == x.hashCode() && obj.equals(x)) {
        arr[i] = null;
        return true;
      }
    }
    return false;
  }
  public static <T> T[] copyWithoutNulls( T[] arr ){
    if( arr == null ) return null;
    List<T> newArr = new ArrayList<>( arr.length /2 );
    for(int i = 0; i < arr.length; i++){
      if( arr[i] != null ) newArr.add( arr[i] );
    }
    return newArr.toArray(arr);
  }
  
  /**
   * Este método é usado internamente para criar os itens de filtragem das
   * buscas.
   *
   * @param uriQuery
   * @return
   */
  public static String[][] parseQueryString(String uriQuery) {
    // Interpretando "Query String" (parâmetros de busca no banco [WHERE])
    //List<String[]> querysPs = new ArrayList<>();
    int qI = 0, qLimit = 30;
    String[][] querysPs = new String[qLimit][];
    if (uriQuery != null) {
      Matcher m = queryStringPattern.matcher(uriQuery);
      while (m.find()) {
        String attr = m.group(1);
        String comp = m.group(2);
        String valor = m.group(3);
        String opComp = m.group(5);
        if (attr == null || comp == null || valor == null) {
          continue;
        }

        boolean isValor = valorPattern.matcher(valor).find();
        if (isValor) {
          valor = valor.substring(1, valor.length() - 1);
        }

        String[] params = {attr, comp, valor, opComp, isValor ? "" : null};
        //querysPs.add(params);
        querysPs[qI++] = params;
      }
    }
    String[][] resQueryPs = new String[qI][];
    System.arraycopy(querysPs, 0, resQueryPs, 0, qI);
    return resQueryPs;
  }
  
  
  public static Set<SingularAttribute> getIds(EntityManager em, Class<?> klass){
    Metamodel meta = em.getMetamodel();
    EntityType entity = meta.entity( klass );
    
    try{
      return entity.getIdClassAttributes();
    }catch(IllegalArgumentException ex){
      Set<SingularAttribute> ids = new HashSet<>(4);
      for( Attribute attr : (Set<Attribute>)entity.getAttributes() ){
        Field field = (Field)attr.getJavaMember();
        Id id = field.getAnnotation( Id.class );
        if( id != null ){
          ids.add( (SingularAttribute)attr );
        }
        /*
        EmbeddedId embId = field.getAnnotation( EmbeddedId.class );
        if( embId != null ){
          Class c = attr.getJavaType();
          try{
            for( Field f : c.getDeclaredFields() ){
              id = f.getAnnotation(Id.class);
              if( id != null ){
                ids.add( (SingularAttribute)attr );
                continue;
              }
            }
          }catch(SecurityException exS){
            
          }
        }
          /* */
      }
    }
    
    return null;
  }
  
  
}
