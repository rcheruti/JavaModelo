
package br.eng.rcc.framework.jaxrs.persistencia;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface CriteriaWhereBuilder {
    
    Predicate apply(CriteriaBuilder cb, Expression exp1, Comparable exp2);
    
}
