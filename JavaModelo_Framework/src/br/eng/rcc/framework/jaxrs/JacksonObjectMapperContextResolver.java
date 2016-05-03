package br.eng.rcc.framework.jaxrs;

import br.eng.rcc.framework.seguranca.filtros.JacksonAnnIntrospector;
import br.eng.rcc.framework.seguranca.filtros.JacksonFilter;
import br.eng.rcc.framework.utils.CdiUtils;
import br.eng.rcc.framework.utils.ClassCache;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class JacksonObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
  
  private ObjectMapper omCache;
  
  @Override
  public ObjectMapper getContext(Class<?> type) {
    if( omCache == null ){
      omCache = criar();
    }
    return omCache;
  }

  private ObjectMapper criar() {
    ObjectMapper mapper = new ObjectMapper();

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

    // outras configurações:
    mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    
    
      // pegando da CDI:
    JacksonFilter filter = CdiUtils.getBean(JacksonFilter.class);
    if( filter != null ){
      mapper.setFilters( new SimpleFilterProvider()
              .addFilter("seguranca", filter) 
      );
    }
    ClassCache cache = CdiUtils.getBean(ClassCache.class);
    if( cache != null ){
      mapper.setAnnotationIntrospector( new JacksonAnnIntrospector(cache) );
    }
    
    
    //mapper.enableDefaultTyping(  );
    return mapper;
  }

}
