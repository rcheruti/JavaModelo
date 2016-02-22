
package br.eng.rcc.framework.seguranca.config;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SegurancaNode implements Seguranca, Serializable{
  
  private String permissao;
  private String grupo;
  private boolean select = true;
  private boolean insert = true;
  private boolean update = true;
  private boolean delete = true;
  private Class<? extends Supplier<? extends Predicate>> predicado;
  private Class<? extends SegurancaPersistenciaInterceptador> persistenciaInsert;
  private Class<? extends SegurancaPersistenciaInterceptador> persistenciaUpdate;
  private Class<? extends SegurancaPersistenciaInterceptador> persistenciaSelect;
  private Class<? extends SegurancaPersistenciaInterceptador> persistenciaDelete;
  
  
  public void setPermissao(String x){ this.permissao = x; }
  public String getPermissao(){ return this.permissao; }
  
  public void setGrupo(String x){ this.grupo = x; }
  public String getGrupo(){ return this.grupo; }
  
  public void setSelect(boolean x){ this.select = x; }
  public boolean getSelect(){ return this.select; }
  
  public void setInsert(boolean x){ this.insert = x; }
  public boolean getInsert(){ return this.insert; }
  
  public void setUpdate(boolean x){ this.update = x; }
  public boolean getUpdate(){ return this.update; }
  
  public void setDelete(boolean x){ this.delete = x; }
  public boolean getDelete(){ return this.delete; }
  
  @Override
  public String value() {
    return permissao;
  }

  @Override
  public String grupo() {
    return grupo;
  }

  @Override
  public boolean select() {
    return select;
  }

  @Override
  public boolean insert() {
    return insert;
  }

  @Override
  public boolean update() {
    return update;
  }

  @Override
  public boolean delete() {
    return delete;
  }

  @Override
  public Class<? extends Supplier<? extends Predicate>> predicado() {
    return predicado;
  }

  @Override
  public Class<? extends SegurancaPersistenciaInterceptador> persistenciaInsert() {
    return persistenciaInsert;
  }

  @Override
  public Class<? extends SegurancaPersistenciaInterceptador> persistenciaUpdate() {
    return persistenciaUpdate;
  }

  @Override
  public Class<? extends SegurancaPersistenciaInterceptador> persistenciaDelete() {
    return persistenciaDelete;
  }

  @Override
  public Class<? extends SegurancaPersistenciaInterceptador> persistenciaSelect() {
    return persistenciaSelect;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Seguranca.class;
  }
  
  
  
}
