
package br.eng.rcc.framework.websocket;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@ApplicationScoped
public class StatusProducer {
    
    @Inject
    private HttpServletRequest req;
    
    @Produces
    @Dependent
    public Status produzir(  ){
        if( req == null ) return null;
        HttpSession session = req.getSession(false);
        if( session == null ) return null;
        Status st = (Status) session.getAttribute( Status.class.getName() );
        if( st != null ) return st;
        synchronized( session ){
            st = (Status) session.getAttribute( Status.class.getName() );
            if( st == null ){
                st = new Status();
                session.setAttribute( Status.class.getName() , st);
            }
        }
        return st;
    }
    
}
