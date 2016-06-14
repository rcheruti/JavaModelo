
package br.eng.rcc.framework.seguranca.filtros;

import br.eng.rcc.framework.persistencia.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.anotacoes.Segurancas;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.utils.ClassCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class JacksonFilter implements PropertyFilter{
  
  @Inject
  private SegurancaServico checker;
  @Inject
  private ClassCache cache;
  
  @Override
  public void serializeAsField(Object o, JsonGenerator jg, SerializerProvider sp, 
          PropertyWriter writer) throws Exception {
    //System.out.printf("---  JacksonFilter:serializeAsField: %s \n", writer.getName() );
    
    Seguranca[] segsAnn = null;
    Segurancas annSegs = writer.getAnnotation( Segurancas.class );
    if( annSegs == null || annSegs.value().length == 0 ){
      Seguranca annSeg = writer.getAnnotation( Seguranca.class );
      if( annSeg != null ){
        segsAnn = new Seguranca[]{ annSeg };
      }
    }else{
      segsAnn = annSegs.value();
    }
    
    if( segsAnn == null || segsAnn.length == 0 ){
      writer.serializeAsField(o, jg, sp);
    }else{
      try{
        checker.check( segsAnn , Seguranca.SELECT );
        Class<?> klass = cache.getUtil( o.getClass().getSimpleName() )
                .path( writer.getName() ).getJavaType();
        if( klass != null ) checker.check(klass, Seguranca.SELECT);
        writer.serializeAsField(o, jg, sp);
      }catch(MsgException ex){
        //System.out.printf("---  ---  Bloqueando!: %s \n", ex.getMensagem() );
      }
    }
    
  }

  @Override
  public void serializeAsElement(Object o, JsonGenerator jg, SerializerProvider sp, 
          PropertyWriter writer) throws Exception {
    //System.out.printf("---  JacksonFilter:serializeAsElement: %s \n", writer.getName() );
    
    
    Seguranca[] segsAnn = null;
    Segurancas annSegs = writer.getAnnotation( Segurancas.class );
    if( annSegs == null || annSegs.value().length == 0 ){
      Seguranca annSeg = writer.getAnnotation( Seguranca.class );
      if( annSeg != null ){
        segsAnn = new Seguranca[]{ annSeg };
      }
    }else{
      segsAnn = annSegs.value();
    }
    
    if( segsAnn == null || segsAnn.length == 0 ){
      writer.serializeAsElement(o, jg, sp);
    }else{
      try{
        checker.check( segsAnn );
        Class<?> klass = cache.getUtil( o.getClass().getSimpleName() )
                .path( writer.getName() ).getJavaType();
        if( klass != null ) checker.check(klass);
        writer.serializeAsElement(o, jg, sp);
      }catch(MsgException ex){

      }
    }
  }

  @Override
  public void depositSchemaProperty(PropertyWriter writer, ObjectNode on, 
          SerializerProvider sp) throws JsonMappingException {
    writer.depositSchemaProperty(on, sp);
  }

  @Override
  public void depositSchemaProperty(PropertyWriter writer, JsonObjectFormatVisitor jofv, 
          SerializerProvider sp) throws JsonMappingException {
    writer.depositSchemaProperty(jofv);
  }
  
  
  
}
