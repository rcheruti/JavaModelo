
package br.eng.rcc.framework.produtores;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class EMProducer {
    
    @PersistenceContext
    @Produces
    private EntityManager em;
    
}
