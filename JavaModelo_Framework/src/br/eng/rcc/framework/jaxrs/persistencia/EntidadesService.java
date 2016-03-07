package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

@ApplicationScoped
public class EntidadesService {
  
  @Inject
  protected EntityManager em;
  @Inject
  protected SegurancaServico checker;
  
  
  //=====================================================================
  
  protected Map<Class,String> tipagem = new HashMap<>(30);
  {
    tipagem.put( boolean.class, "boolean" );
    tipagem.put( Boolean.class, "boolean" );
    tipagem.put( byte.class, "integer" );
    tipagem.put( Byte.class, "integer" );
    tipagem.put( short.class, "integer" );
    tipagem.put( Short.class, "integer" );
    tipagem.put( int.class, "integer" );
    tipagem.put( Integer.class, "integer" );
    tipagem.put( long.class, "integer" );
    tipagem.put( Long.class, "integer" );
    tipagem.put( float.class, "number" );
    tipagem.put( Float.class, "number" );
    tipagem.put( double.class, "number" );
    tipagem.put( Double.class, "number" );
    tipagem.put( char.class, "string" );
    tipagem.put( Character.class, "string" );
    tipagem.put( String.class, "string" );
    tipagem.put( Date.class, "Date" );
    tipagem.put( Calendar.class, "Date" );
    tipagem.put( Time.class, "Date" );
    tipagem.put( boolean[].class, "Blob" );
  }
  
  //=====================================================================
  
  public JsonResponse tipo(Class<?> klass) {
    checker.check(klass, Seguranca.SELECT | Seguranca.INSERT
            | Seguranca.DELETE | Seguranca.UPDATE);

    Metamodel meta = this.em.getMetamodel();
    ManagedType entity = meta.managedType(klass);

    Map<String, String> map = new HashMap<>(20);
    Set<Attribute> attrs = entity.getDeclaredAttributes();
    for (Attribute attr : attrs) {
      if (attr.isCollection()) {
        PluralAttribute pAttr = (PluralAttribute) attr;
        map.put(pAttr.getName(), String.format("[%s",
                pAttr.getElementType().getJavaType().getSimpleName()));
      } else {
        String tStr = tipagem.get( attr.getJavaType() );
        if( tStr == null ) tStr = attr.getJavaType().getSimpleName();
        map.put(attr.getName(), tStr);
      }
    }

    return new JsonResponse(true, map, "Busca do tipo");
  }

  public void buscar() {

  }

  public void criar() {

  }

  public void editar() {

  }

  public void deletar() {

  }

  //============================================================================
}
