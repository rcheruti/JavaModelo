
package br.eng.rcc.framework.jaxrs.persistence;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;

/**
 *
 * @author rcheruti
 */
@Path("/namedQuery")
@RequestScoped
public class NamedQueryService {
    
    @Inject
    private EntityManager em;
    
    
    
    
}
