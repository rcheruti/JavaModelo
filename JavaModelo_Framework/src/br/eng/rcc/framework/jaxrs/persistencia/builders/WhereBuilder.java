
package br.eng.rcc.framework.jaxrs.persistencia.builders;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;

public class WhereBuilder {
    private static WhereBuilder instance;
    static{
        instance = new WhereBuilder();
    }
    
    private final Map<String, CriteriaWhereBuilder> map;
    private final char likeCuringa = '%';

    private WhereBuilder(){
        final Map<String, CriteriaWhereBuilder> staticMap = new HashMap<>(20);
        staticMap.put("=", ( cb, exp1,  exp2) -> cb.equal(exp1,exp2));
        staticMap.put("!=", ( cb, exp1,  exp2) -> cb.notEqual(exp1,exp2));
        staticMap.put("<=", ( cb, exp1,  exp2) -> cb.greaterThan(exp1,exp2).not());
        staticMap.put(">=", ( cb, exp1,  exp2) -> cb.greaterThanOrEqualTo(exp1,exp2));
        staticMap.put("<", ( cb, exp1,  exp2) -> cb.greaterThanOrEqualTo(exp1,exp2).not());
        staticMap.put(">", ( cb, exp1,  exp2) -> cb.greaterThan(exp1,exp2));
        staticMap.put("notlike", ( cb, exp1,  exp2) -> cb.notLike(exp1,((String)exp2).replaceAll("\\*", "%") ));
        staticMap.put("like", ( cb, exp1,  exp2) -> cb.like(exp1, ((String)exp2).replaceAll("\\*", "%") ));
        staticMap.put("isnull", ( cb, exp1,  exp2) -> cb.isNull(exp1) );
        staticMap.put("isnotnull", ( cb, exp1,  exp2) -> cb.isNotNull(exp1) );
        map = staticMap;
    }
    public static WhereBuilderInterface create(CriteriaBuilder cb, CriteriaQuery query){
        //if( instance == null ) instance = new WhereBuilder();
        return new WhereCriteria(cb, query, instance.map);
    }
    public static WhereBuilderInterface create(CriteriaBuilder cb, CriteriaDelete query){
        //if( instance == null ) instance = new WhereBuilder();
        return new WhereDelete(cb, query, instance.map);
    }
    public static WhereBuilderInterface create(CriteriaBuilder cb, CriteriaUpdate query){
        //if( instance == null ) instance = new WhereBuilder();
        return new WhereUpdate(cb, query, instance.map);
    }
    
    
}