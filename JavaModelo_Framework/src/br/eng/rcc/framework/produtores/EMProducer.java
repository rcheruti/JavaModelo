
package br.eng.rcc.framework.produtores;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

@ApplicationScoped
public class EMProducer {
    
    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    @Produces
    private EntityManager em;
    
}
