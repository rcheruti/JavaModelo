
package br.eng.rcc.framework.utils;

import java.lang.reflect.InvocationTargetException;

public interface IBeanUtil {
  
  /**
   * @return o valor guardado nesse atributo
   * @param from o objeto onde será chamado o getter
   * @param paths o caminho (atributo) que deverá ser seguido para achar o utilitário
   *    do atributo
   * 
   * @throws IllegalAccessException pela classe que representa o método da reflexão
   * @throws InvocationTargetException pela classe que representa o método da reflexão
   */
  default Object get(Object from, String... paths)
          throws IllegalAccessException, InvocationTargetException{
    return get(from, 0, paths);
  }
  Object get(Object from, int index, String... paths)
          throws IllegalAccessException, InvocationTargetException;
  
  
  /**
   * Configura o valor que deve ser guardado nesse atributo.
   * @param from o objeto onde será chamado o setter
   * @param val o valor que será guardado no objeto
   * @param paths o caminho (atributo) que deverá ser seguido para achar o utilitário
   *    do atributo
   * 
   * @throws IllegalAccessException pela classe que representa o método da reflexão
   * @throws InvocationTargetException pela classe que representa o método da reflexão
   */
  default void set(Object from, Object val, String... paths)
          throws IllegalAccessException, InvocationTargetException{
    set(from, val, 0, paths);
  }
  void set(Object from, Object val, int index, String... paths)
          throws IllegalAccessException, InvocationTargetException;
  
  
  /**
   * @return o nome desse atributo/classe, como estará definido no EntityManager.s
   */
  String getNome();
  /**
   * @return a classe que este objeto representa
   */
  Class getJavaType();
  
  /**
   * @param paths o caminho (ou caminhos) que deve ser esguido para chegar no
   *    utilitário que está sendo procurado. Essas Strings são os nomes dos atributos 
   *    na árvore que está sendo pesquisada (na árvore de classes).
   * @return o próximo utilitário deste caminho
   */
  default IBeanUtil path(String... paths){
    return path(0, paths);
  }
  IBeanUtil path(int index, String... paths);
  
  
}
