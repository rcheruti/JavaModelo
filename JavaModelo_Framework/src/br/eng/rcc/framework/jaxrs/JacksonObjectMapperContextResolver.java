
package br.eng.rcc.framework.jaxrs;

import br.eng.rcc.framework.jaxrs.persistence.ClassCache;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

//@Provider
//@RequestScoped
@ApplicationScoped
public class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper>{
    
    @Inject
    private EntityManager em;
    @Inject
    private ClassCache cache;
    @Context
    private UriInfo uriInfo;
    
    private Pattern manyReq = Pattern.compile("^.*/s/persistence/many(?:[\\?;].*)?$");
    
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
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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
    
    
    //-----------------  Módulo para configura o ObjectMapper  ----------------
    
    private class ObjectMapperJsonNodeModule extends Module {

        @Override
        public String getModuleName() {
            return ObjectMapperJsonNodeModule.class.getSimpleName();
        }

        @Override
        public Version version() {
            return new Version( 1, 0, 0, 
                    "ObjectMapperJsonNodeModule-info", 
                    "ObjectMapperJsonNodeModule-group", 
                    "ObjectMapperJsonNodeModule-artifact" ) ;
        }

        @Override
        public void setupModule(SetupContext sc) {
            
            SimpleAbstractTypeResolver absResolver = new SimpleAbstractTypeResolver();
            absResolver.addMapping(Collection.class, ArrayList.class);
            absResolver.addMapping(List.class, ArrayList.class);
            absResolver.addMapping(Set.class, HashSet.class);
            absResolver.addMapping(Map.class, HashMap.class);
            
            
            sc.addTypeModifier( new CustomTypeModifier() );
            sc.addAbstractTypeResolver(absResolver);
            //sc.addBeanDeserializerModifier( new BeanDeserializerModifierImpl() );
        }
        
    }
    
    private class CustomTypeModifier extends TypeModifier {

        @Override
        public JavaType modifyType(JavaType jt, Type type, TypeBindings tb, TypeFactory tf) {
            if( jt.getRawClass() == Object.class ){
                
                List<String> lista = uriInfo.getPathParameters().get("entidade");
                if( lista != null ) for( String entidade : lista ){
                    Class<?> klass =  cache.get(entidade, em);
                    if( klass != null ){
                        System.out.printf("=====  entidade: %s , klass: %s \n", entidade, klass);
                        return tf.constructType( klass );
                    }
                }
                
            }
            return jt ;
        }
        
    }
    
}
