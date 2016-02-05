package br.eng.rcc.framework.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

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
        if (!constainsInArray(params, name)) {
          fieldsToNullify.add(member);
        } else {
          // Descer a arvore para nullify
          fieldsDownNullify.add(member);
        }
      }

      // Aplicar valor NULL nas lista que são LAZY:
      for (Field field : fieldsToNullify) {
        for (Object obj : lista) {
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
          try {
            Object campoValor = field.get(obj) ;
            if( campoValor instanceof Collection ){
              nullifyLazy(em, ((Collection)campoValor).toArray(),
                    new String[0], secureLevel);
            }else{
              Object[] campoValorArr = { campoValor };
              nullifyLazy(em, campoValorArr,
                    new String[0], secureLevel);
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
    for (Object x : arr) {
      if (hash == x.hashCode() && obj.equals(x)) {
        return true;
      }
    }
    return false;
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

}
