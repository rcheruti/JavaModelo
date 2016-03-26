package br.eng.rcc.framework.persistencia.builders;

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
        preds[len] = map.get(vet[1]).apply(cb, exp1, vet[2]);
      } catch (IllegalArgumentException ex) {
      }
      len++;
    }
    return preds;
  }

}
