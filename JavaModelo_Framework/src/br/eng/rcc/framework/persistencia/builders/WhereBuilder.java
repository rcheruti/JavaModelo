package br.eng.rcc.framework.persistencia.builders;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class WhereBuilder {

  private static final Map<String, CriteriaWhereBuilder> map;

  static{
    final Map<String, CriteriaWhereBuilder> staticMap = new HashMap<>(20);
    staticMap.put("=", (cb, exp1, exp2) -> cb.equal(exp1, exp2));
    staticMap.put("!=", (cb, exp1, exp2) -> cb.notEqual(exp1, exp2));
    staticMap.put("<=", (cb, exp1, exp2) -> cb.greaterThan(exp1, exp2).not());
    staticMap.put(">=", (cb, exp1, exp2) -> cb.greaterThanOrEqualTo(exp1, exp2));
    staticMap.put("<", (cb, exp1, exp2) -> cb.greaterThanOrEqualTo(exp1, exp2).not());
    staticMap.put(">", (cb, exp1, exp2) -> cb.greaterThan(exp1, exp2));
    staticMap.put("notlike", (cb, exp1, exp2) -> cb.notLike(exp1, ((String) exp2).replaceAll("\\*", "%")));
    staticMap.put("like", (cb, exp1, exp2) -> cb.like(exp1, ((String) exp2).replaceAll("\\*", "%")));
    staticMap.put("isnull", (cb, exp1, exp2) -> cb.isNull(exp1));
    staticMap.put("isnotnull", (cb, exp1, exp2) -> cb.isNotNull(exp1));
    map = staticMap;
  }


  public static Predicate[] build(CriteriaBuilder cb, Root root, String[][] arr) {
    int len = 0;
    Predicate[] preds = new Predicate[arr.length];
    for (String[] vet : arr) {
      if (vet == null
              || vet[0] == null
              || vet[1] == null
              || vet[2] == null) {
        continue;
      }
      try {
        String[] vet0 = vet[0].split("\\.");
        Path exp1 = root.get(vet0[0]);
        for (int i = 1; i < vet0.length; i++) {
          exp1 = exp1.get(vet0[i]);
        }
        preds[len] = map.get(vet[1]).apply(cb, exp1,  as(exp1,vet[2]) );
      } catch (IllegalArgumentException ex) {
      }
      len++;
    }
    return preds;
  }
  
  
  
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat();
  
  private static Comparable as( Path exp1, String val ){
    if( exp1 == null || val == null ) return null;
    Class tipo = exp1.getJavaType();
    if( Boolean.class.isAssignableFrom(tipo) || boolean.class.isAssignableFrom(tipo) ) return Boolean.valueOf(val);
    if( Byte.class.isAssignableFrom(tipo) || byte.class.isAssignableFrom(tipo) ) return Byte.valueOf(val);
    if( Character.class.isAssignableFrom(tipo) || char.class.isAssignableFrom(tipo) ) return val.isEmpty()?'\0':val.charAt(0);
    if( Short.class.isAssignableFrom(tipo) || short.class.isAssignableFrom(tipo) ) return Short.valueOf(val);
    if( Integer.class.isAssignableFrom(tipo) || int.class.isAssignableFrom(tipo) ) return Integer.valueOf(val);
    if( Long.class.isAssignableFrom(tipo) || long.class.isAssignableFrom(tipo) ) return Long.valueOf(val);
    if( Float.class.isAssignableFrom(tipo) || float.class.isAssignableFrom(tipo)) return Float.valueOf(val);
    if( Double.class.isAssignableFrom(tipo) || double.class.isAssignableFrom(tipo)) return Double.valueOf(val);
    if( String.class.isAssignableFrom(tipo) ) return val;
    if( BigInteger.class.isAssignableFrom(tipo) ) return new BigInteger(val);
    if( BigDecimal.class.isAssignableFrom(tipo) ) return new BigDecimal(val);
    try{
      if( Date.class.isAssignableFrom(tipo) ) return dateFormat.parse(val);
      if( Calendar.class.isAssignableFrom(tipo) ){
        Calendar ca = Calendar.getInstance();
        ca.setTime( dateFormat.parse(val) );
        return ca;
      }
      if( Time.class.isAssignableFrom(tipo) ) return new Time( dateFormat.parse(val).getTime() );
    }catch(ParseException ex){
      throw new MsgException(JsonResponse.ERROR_EXCECAO,null,"Formato de Date errado", ex);
    }
    
    return null;
  }
  
  
}
