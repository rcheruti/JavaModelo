
package br.eng.rcc.framework.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionMapeador implements ExceptionMapper<MsgException>{
    
    @Override
    public Response toResponse(MsgException ex) {
        return Response.ok( new JsonResponse(false, ex.getData(), ex.getMessage()) ).build() ;
    }
    
}
