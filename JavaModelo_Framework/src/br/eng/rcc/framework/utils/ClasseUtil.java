
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
  public IBeanUtil path(String... paths){
    return path( 0, paths );
  }
  @Override
  public IBeanUtil path(int idx, String... paths){
    if( idx == 0 && paths.length == 1 ){
      paths = paths[0].split("\\.");
    }
    
    String meuPath = paths[idx];
    ClasseAtributoUtil util = mapaAtributos.get( meuPath );
    if( util == null ) return null;
    
    idx++;
    if( idx < paths.length ) return util.path(idx, paths); 
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
  public Object get(Object from, String... path)
          throws IllegalAccessException, InvocationTargetException{
    
    return null;
  }
  @Override
  public void set(Object from, Object val, String... path)
          throws IllegalAccessException, InvocationTargetException{
    
  }
  
  
}
