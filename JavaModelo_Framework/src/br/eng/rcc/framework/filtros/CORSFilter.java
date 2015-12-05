
package br.eng.rcc.framework.filtros;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class CORSFilter implements ContainerResponseFilter{

    @Override
    public void filter(ContainerRequestContext crc, 
            ContainerResponseContext crc1) throws IOException {
        crc1.getHeaders().add("Access-Control-Allow-Origin", "*");
        crc1.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, X-Requested-With");
        crc1.getHeaders().add("Access-Control-Allow-Credentials", "true");
        crc1.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        crc1.getHeaders().add("Access-Control-Max-Age", "12000");
        crc1.getHeaders().add("Cache-Control", "no-cache");
    }
    
}
