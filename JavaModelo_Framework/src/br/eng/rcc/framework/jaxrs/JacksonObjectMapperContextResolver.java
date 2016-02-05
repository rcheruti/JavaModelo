
package br.eng.rcc.framework.jaxrs;

import br.eng.rcc.framework.jaxrs.persistencia.ClassCache;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper>{
        
    private Pattern manyReq = Pattern.compile("^.*/s/persistencia/many(?:[\\?;].*)?$");
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        ObjectMapper mapper = new ObjectMapper();
        //boolean isManyFrameworkRequest = manyReq.matcher( uriInfo.getPath() ).find() ;
        boolean isManyFrameworkRequest = false ;
        
        // para serialize:
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        
        // para deserialize:
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        //mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        //mapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        if( isManyFrameworkRequest ){
            System.out.println("---->>>   Many request!! ");
            mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        }
        
        // outras configurações:
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        
        //mapper.enableDefaultTyping(  );
        
        //mapper.registerModule( new ObjectMapperJsonNodeModule() );
        return mapper;
    }
    
}
