
package br.eng.rcc.framework.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Essa classe tem funções que facilitam a interação com objetos da classe que
 * este utilitário representa.
 * <br><br>
 * Essa classe (e as classes que implementam {@link IBeanUtil}) fazem o 
 * trabalho complicado de busca e reflexão na árvore de entidades.
 * 
 */
public class ClasseUtil implements IBeanUtil{
  
  /**
   * Nome da classe/entidade, como estará definido no cache do EntityManager.
   */
  protected String nome;
  /**
   * Classe que este utilitário representa.
   */
  protected Class<?> javaType;
  
  protected Map<String, ClasseAtributoUtil> mapaAtributos = new HashMap<>();
  
  
  @Override
  public IBeanUtil path(int index, String... paths){
    if( index > -1 && paths.length == 1 ){
      paths = paths[0].split("\\.");
    }
    if( index < 0 ) index *= -1;
    
    String meuPath = paths[index];
    ClasseAtributoUtil util = mapaAtributos.get( meuPath );
    if( util == null ) return null;
    
    if( index < paths.length -1 ) return util.path(0).path(index +1, paths); 
    return util;
  }
  
  
  /**
   * @return a classe que este utilitário representa
   */
  @Override
  public Class<?> getJavaType(){ return javaType; }
  /**
   * @return o nome dessa classe/entidade, como estará definido no EntityManager
   */
  @Override
  public String getNome(){ return nome; }
  
  
  @Override
  public Object get(Object from, int index, String... paths)
          throws IllegalAccessException, InvocationTargetException{
    if( from == null ) return null;
    if( index > -1 && paths.length == 1 ){
      paths = paths[0].split("\\.");
    }
    if( index < 0 ) index *= -1;
    if( paths.length == 0 || index >= paths.length ) return null;
    ClasseAtributoUtil util = mapaAtributos.get( paths[index] );
    if( util == null ) return null;
    return util.get(from, index , paths);
  }
  @Override
  public void set(Object from, Object val, int index, String... paths)
          throws IllegalAccessException, InvocationTargetException{
    if( index > -1 && paths.length == 1 ){
      paths = paths[0].split("\\.");
    }
    if( index < 0 ) index *= -1;
    if( paths.length == 0 || index >= paths.length ) return;
    ClasseAtributoUtil util = mapaAtributos.get( paths[index] );
    if( util == null ) return;
    util.set(from, val, index , paths);
  }
  
  
}
