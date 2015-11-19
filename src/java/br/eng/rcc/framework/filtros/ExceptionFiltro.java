
package br.eng.rcc.framework.filtros;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.JsonResponseWriter;
import br.eng.rcc.framework.jaxrs.MsgException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.ws.rs.core.MediaType;

@WebFilter("/*")
public class ExceptionFiltro implements Filter{
    
    @Inject
    private JsonResponseWriter writer;
    
    //======================================================
    
    @Override
    public void init(FilterConfig fc) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) 
            throws IOException, ServletException {
        try{
            fc.doFilter(sr, sr1);
        }catch(Exception ex){
            if( ex instanceof MsgException ){
                
            }
            
            //System.out.println("--- Exceção tipo: "+ ex.getClass() );
            //Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsonResponse res = new JsonResponse(false, ex.getMessage());
            writer.writeTo(res, res.getClass(), null, 
                        new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, 
                        sr1.getOutputStream() );
            /*
            new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValue( sr1.getOutputStream(), res);
            /* */
        }/* */
    }

    @Override
    public void destroy() {
        
    }
    
}
