
package br.eng.rcc.framework.jaxrs.persistencia.builders;

import javax.persistence.criteria.Predicate;

public interface WhereBuilderInterface {
    
    Predicate[] build();
    <T extends WhereBuilderInterface> T addArray( String[][] arr );
    <T extends WhereBuilderInterface> T add(String[] vet);
    
}
