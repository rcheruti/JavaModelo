
package br.eng.rcc.framework.jaxrs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class JsonRequestReader implements MessageBodyReader<Object>{
    
    //@Context
    //private UriInfo uriInfo;
    //@Inject
    //private ClassCache cache;
    
    @Override
    public boolean isReadable(Class<?> type, 
                            Type genericType, 
                            Annotation[] annotations, 
                            MediaType mediaType){
        if( JsonNode.class.equals(type) ){
            return true;
        }else if( Object.class.equals(type) ){
            //String entidadeName = uriInfo.getPathParameters().get("entidade").get(0);
            //Class<?> klass = cache.get(entidadeName);
            //if( klass == null ) return false;
        }
        return true;
    }

    @Override
    public Object readFrom(Class<Object> type, 
                        Type genericType, 
                        Annotation[] annotations, 
                        MediaType mediaType, 
                        MultivaluedMap<String, String> httpHeaders, 
                        InputStream entityStream) 
            throws IOException, WebApplicationException {
        if( JsonNode.class.equals(type) ){
            Object x = new ObjectMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                    .readTree(entityStream);
            return x;
        }else if( Object.class.equals(type) ){
            //String entidadeName = uriInfo.getPathParameters().get("entidade").get(0);
            //type = (Class<Object>) cache.get(entidadeName);
        }
        try{
            Object x = new ObjectMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                    .readValue(entityStream, type);
            return x;
        }catch(Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    

}
