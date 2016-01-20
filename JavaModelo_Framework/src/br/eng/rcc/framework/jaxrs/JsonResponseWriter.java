
package br.eng.rcc.framework.jaxrs;

import br.eng.rcc.framework.config.Configuracoes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;


@Provider
@Produces({ "*/*" })
@ApplicationScoped
public class JsonResponseWriter implements MessageBodyWriter<Object>{
    
    //@Context
    //private Providers providers;
    @Inject
    private JacksonObjectMapperContextResolver resolver;
    
    @Override
    public boolean isWriteable( Class<?> type, 
                                Type genericType, 
                                Annotation[] annotations, 
                                MediaType mediaType) {
        return type == JsonResponse.class;
    }

    @Override
    public long getSize(    Object t, 
                            Class<?> type, 
                            Type genericType, 
                            Annotation[] annotations, 
                            MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(    Object t, 
                            Class<?> type, 
                            Type genericType, 
                            Annotation[] annotations, 
                            MediaType mediaType, 
                            MultivaluedMap<String, Object> httpHeaders, 
                            OutputStream entityStream) 
            throws IOException, WebApplicationException {
        if( httpHeaders != null ){
          List<Object> lista = httpHeaders.get("Content-Type") ;
          if( lista == null ){
              lista = new ArrayList<>(4);
              httpHeaders.put("Content-Type", lista);
          }
          lista.add(MediaType.APPLICATION_JSON+"; charset=utf-8");
        }
        
        byte[] json;
        try{
            ObjectMapper mapper = resolver.getContext(ObjectMapper.class);
            json = mapper.writeValueAsBytes(t);
        }catch(JsonProcessingException ex){
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
            json = new byte[0];
        }
        
        entityStream.write(json);
    }
    
    
    
    
    
    public String toJson(Object obj){
        try( ByteArrayOutputStream stream = new ByteArrayOutputStream(512); ){
            this.writeTo(obj, obj.getClass(), null, null, MediaType.WILDCARD_TYPE, 
                    new MultivaluedHashMap<>(1), stream);
            String s = stream.toString();
            return s;
        }catch(IOException ex){ 
            return ""; 
        }
    }
    
    
}
