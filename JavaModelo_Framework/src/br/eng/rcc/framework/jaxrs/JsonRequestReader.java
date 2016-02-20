package br.eng.rcc.framework.jaxrs;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.persistencia.ClassCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Provider
@RequestScoped
@Consumes({Configuracoes.JSON_PERSISTENCIA, MediaType.APPLICATION_JSON})
@Priority(Integer.MAX_VALUE) // Tenta substituir os provedores padrão (Jackson/Jettison)
public class JsonRequestReader implements MessageBodyReader<Object> {

  @Context
  private UriInfo uriInfo;
  @Inject
  private ClassCache cache;
  @Inject
  private JacksonObjectMapperContextResolver resolver;
  
  private static final Pattern persistenciaPattern = Pattern.compile(
    "/persistencia(?!/many)");

  @Override
  public boolean isReadable(Class<?> type,
          Type genericType,
          Annotation[] annotations,
          MediaType mediaType) {
    /*
    if (JsonNode.class.equals(type)) {
      return true;
    } else if (Object.class.equals(type) || Collection.class.isAssignableFrom(type)) {
      List<String> params = uriInfo.getPathParameters().get("entidade");
      String entidadeName = params.get(0);
      Class<?> klass = cache.get(entidadeName, em);
      if (klass == null) {
        return false;
      }
    }
    */
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
    
    if (JsonNode.class.equals(type)) {
      Object x = fromJson(entityStream, type);
      return x;
    } 
    
    Class<? extends Collection> typeList = 
                List.class.isAssignableFrom(type)? List.class 
              : Set.class.isAssignableFrom(type)? Set.class
              : Collection.class.isAssignableFrom(type)? List.class // padrão para list
              : null; // não é lista
    
    Matcher matcher = persistenciaPattern.matcher( uriInfo.getPath() );
    if( matcher.find() ){
      List<String> params = uriInfo.getPathParameters().get("entidade");
      if( params != null && params.size() > 0 ){
        type = (Class<Object>) cache.get( params.get(0) );
      }
    }
    
    System.out.println("Disponivel: "+ entityStream.available() );
    try {
      Object x = fromJson(entityStream, typeList, type);
      return x;
    } catch (UnrecognizedPropertyException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING, ex.getMessage() );
      throw new MsgException( ex.getMessage(), ex );
    } catch (Exception ex) {
      //Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
      return null;
    }
  }

  //========================================================================
  /*
        Esse método existe para criar uma forma padrão de leitura do JSON.
        será movido para outro lugar mais tarde.
   */
  public Object fromJson(InputStream entityStream, Class<Object> type) throws IOException {
    //ContextResolver resolver = providers.getContextResolver(ObjectMapper.class,  
    //                                                    MediaType.WILDCARD_TYPE);
    ObjectMapper mapper = resolver.getContext(ObjectMapper.class);
    return mapper.readValue(entityStream, type);
  }

  public Object fromJson(InputStream entityStream, Class<? extends Collection> typeList, Class<Object> type) throws IOException {
    if (typeList == null) {
      return fromJson(entityStream, type);
    }
    //ContextResolver resolver = providers.getContextResolver(ObjectMapper.class,  
    //                                                    MediaType.WILDCARD_TYPE);
    ObjectMapper mapper = resolver.getContext(ObjectMapper.class);
    return mapper.readValue(entityStream, mapper.getTypeFactory().constructCollectionType(typeList, type));
  }

  public Object fromJson(Reader entityStream, Class<? extends Object> type) throws IOException {
    //ContextResolver resolver = providers.getContextResolver(ObjectMapper.class, 
    //                                                    MediaType.WILDCARD_TYPE);
    ObjectMapper mapper = resolver.getContext(ObjectMapper.class);
    return mapper.readValue(entityStream, type);
  }

  public Object fromJson(Reader entityStream, Class<? extends Collection> typeList, Class<? extends Object> type) throws IOException {
    if (typeList == null) {
      return fromJson(entityStream, type);
    }
    //ContextResolver resolver = providers.getContextResolver(ObjectMapper.class, 
    //                                                    MediaType.WILDCARD_TYPE);
    ObjectMapper mapper = resolver.getContext(ObjectMapper.class);
    return mapper.readValue(entityStream, mapper.getTypeFactory().constructCollectionType(typeList, type));
  }

}
