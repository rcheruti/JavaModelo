
package br.eng.rcc.framework.jaxrs;

import br.eng.rcc.framework.jaxrs.persistence.ClassCache;
import br.eng.rcc.seguranca.entidades.Usuario;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static javafx.scene.input.KeyCode.T;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
@RequestScoped
public class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper>{
    
    @Inject
    private EntityManager em;
    @Inject
    private ClassCache cache;
    @Context 
    private UriInfo uriInfo;
    
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        ObjectMapper mapper = new ObjectMapper();
        
        // para serialize:
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        
        // para deserialize:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        //mapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        //mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        
        // outras configurações:
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        
        //mapper.enableDefaultTyping(  );
        
        //mapper.registerModule( new ObjectMapperJsonNodeModule() );
        return mapper;
    }
    
    
    //-----------------  Módulo para configura o ObjectMapper  ----------------
    public class CustomTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
        
        public CustomTypeResolverBuilder(ObjectMapper.DefaultTyping t) {
            super(t);
        }
        
    }
    
    
    public class CustomAsWrapperTypeDeserializer extends AsWrapperTypeDeserializer{
        
        public CustomAsWrapperTypeDeserializer(
                JavaType bt, TypeIdResolver idRes, 
                String typePropertyName, 
                boolean typeIdVisible, Class<?> defaultImpl) {
            super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }
        
        @Override
        public Object deserializeTypedFromObject(JsonParser parser, DeserializationContext ctxt){
            return null; 
        }
        
    }
    
    
    
    
    
    
    
    public class CustomTypeResolverBuilder_1 implements TypeResolverBuilder<CustomTypeResolverBuilder_1> {
        
        private Class<?> type;
        
        public CustomTypeResolverBuilder_1(Class<?> type){
            this.type = type;
            this.type = Usuario.class;
        }
        
        
        @Override
        public Class<?> getDefaultImpl() {
            return Object.class ;
        }

        @Override
        public TypeSerializer buildTypeSerializer(SerializationConfig sc, JavaType jt, Collection<NamedType> clctn) {
            return new AsWrapperTypeSerializer(null, null);
        }

        @Override
        public TypeDeserializer buildTypeDeserializer(DeserializationConfig dc, JavaType jt, Collection<NamedType> clctn) {
            return new AsWrapperTypeDeserializer(jt, null, null, true, type);
        }

        @Override
        public CustomTypeResolverBuilder_1 init(JsonTypeInfo.Id id, TypeIdResolver tir) {
            
            return this;
        }

        @Override
        public CustomTypeResolverBuilder_1 inclusion(JsonTypeInfo.As as) {
            
            return this;
        }

        @Override
        public CustomTypeResolverBuilder_1 typeProperty(String string) {
            
            return this;
        }

        @Override
        public CustomTypeResolverBuilder_1 defaultImpl(Class<?> type) {
            
            return this;
        }

        @Override
        public CustomTypeResolverBuilder_1 typeIdVisibility(boolean bln) {
            
            return this;
        }
        
    }
    /* */
    
    public class CustomTypeDeserializer extends TypeDeserializer {

        @Override
        public TypeDeserializer forProperty(BeanProperty bp) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext dc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext dc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser jp, DeserializationContext dc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext dc) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getPropertyName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public TypeIdResolver getTypeIdResolver() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Class<?> getDefaultImpl() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    /* */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
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
