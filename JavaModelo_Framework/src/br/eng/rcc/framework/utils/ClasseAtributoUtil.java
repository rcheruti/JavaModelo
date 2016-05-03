
package br.eng.rcc.framework.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClasseAtributoUtil implements IBeanUtil{  


  /**
   * Nome do atributo que esse utilitário representa
   */
  protected String nome;
  /**
   * Informa se esse atributo é uma associação segundo o {@link EntityManager}
   */
  protected boolean associacao;
  /**
   * Informa se esse atributo é uma coleção segundo o {@link EntityManager}
   */
  protected boolean colecao;
  /**
   * Informa se esse atributo é uma relação embutidade segundo o {@link EntityManager}
   */
  protected boolean embutido;
  /**
   * Guarda o método <b>getter</b> desse atributo <i>(JavaBeans)</i>
   */
  protected Method getter;
  /**
   * Guarda o método <b>setter</b> desse atributo <i>(JavaBeans)</i>
   */
  protected Method setter;
  /**
   * Guarda o método <b>add(Object)</b> desse atributo, case ele seja uma coleção
   */
  protected Method add;
  /**
   * Guarda o método <b>remove(Object)</b> desse atributo, case ele seja uma coleção
   */
  protected Method remove;
  /**
   * Guarda a classe do tipo desse atributo, ou o tipo genérico caso ele seja uma coleção
   */
  protected Class javaType;
  /**
   * Guarda a classe da coleção desse atributo, caso ele seja uma coleção
   */
  protected Class colecaoType;
  /**
   * Guarda uma referência para o cache de classes
   */
  protected ClassCache classCache;
  /**
   * Guarda o nome do atributo da inversão (atributo que tem a relação inversa)
   */
  protected String mapeado;

  /**
   * Informa se esse atributo é uma associação segundo o {@link EntityManager}
   * @return <code>true</code> se esse atributo é uma associação
   */
  public boolean isAssociacao(){ return associacao; }
  /**
   * Informa se esse atributo é uma coleção segundo o {@link EntityManager}
   * @return <code>true</code> se esse atributo é uma coleção
   */
  public boolean isColecao(){ return colecao; }
  /**
   * Informa se esse atributo é uma relação embutidade segundo o {@link EntityManager}
   * @return <code>true</code> se esse atributo é uma relação embutida
   */
  public boolean isEmbutido(){ return embutido; }
  /**
   * @return o método <b>getter</b> desse atributo <i>(JavaBeans)</i>
   */
  public Method getGetter(){ return getter; }
  /**
   * @return o método <b>setter</b> desse atributo <i>(JavaBeans)</i>
   */
  public Method getSetter(){ return setter; }
  /**
   * @return o método <b>add(Object)</b> desse atributo, case ele seja uma coleção
   */
  public Method getAdd(){ return add; }
  /**
   * @return o método <b>remove(Object)</b> desse atributo, case ele seja uma coleção
   */
  public Method getRemove(){ return remove; }
  /**
   * @return a classe da coleção desse atributo, caso ele seja uma coleção
   */
  public Class getColecaoType(){ return colecaoType; }
  /**
   * @return o nome do atributo da inversão (o nome em "mappedBy" por exemplo)
   */
  public String getMapeado(){ return mapeado; }
  
  
  
  /**
   * @return a classe do tipo desse atributo, ou o tipo genérico caso ele seja uma coleção
   */
  @Override
  public Class getJavaType(){ return javaType; }
  /**
   * @return o nome do atributo que esse utilitário representa
   */
  @Override
  public String getNome(){ return nome; }
  /**
   * 
   * @return o mapa de utilitários da classe desse atributo, caso esse atributo
   *  seja uma relação de entidades (mesmo que seja uma coleção, onde será 
   *  retornado um mapa do tipo genérico)
   */
  public Map<String,ClasseAtributoUtil> path(){
    return classCache.getInfo(javaType.getSimpleName());
  }
  /**
   * @return o utilitário que representa a inversão dessa relação (ex.: "mappedBy"),
   *  para que seja simples configurar os dois lados da relação
   */
  public ClasseAtributoUtil getInverse(){
    if( !associacao || mapeado == null ) return null;
    Map<String,ClasseAtributoUtil> map = this.path();
    if( map != null ) return map.get(mapeado);
    return null;
  }
  
  
  
  @Override
  public IBeanUtil path(int index, String... paths){
    IBeanUtil util = classCache.getUtil(javaType.getSimpleName());
    if( paths.length == 0 || paths.length == index ) return util;
    return util.path( index, paths );
  }
  
  
  
  /**
   * Uma forma simples de invocar o método <code>getter</code>
   * 
   * @param from o objeto onde a reflexão será aplicada
   * @param index o index para ler de "paths"
   * @param paths o caminho que deve ser seguido para encontrar o atributo
   * @return o valor atual guardado no objeto
   * @throws IllegalAccessException
   * @throws InvocationTargetException 
   */
  @Override
  public Object get(Object from, int index, String... paths) 
          throws IllegalAccessException, InvocationTargetException{ 
    if( from == null ) return null;
    if( index > -1 && paths.length == 1 ){
      paths = paths[0].split("\\.");
    }
    if( index < 0 ) index *= -1;
    if( index < paths.length-1 ){
      Object novoFrom = get(from, -1, paths[index]);
      return path(0).get(novoFrom, index +1, paths);
    }
    if( getter == null ) return null;
    return getter.invoke(from);
  }
  /**
   * Uma forma simples de invocar o método <code>setter</code>
   * 
   * @param from o objeto onde a reflexão será aplicada
   * @param val o valor que será aplicado na reflexão
   * @param index o index para ler de "paths"
   * @param paths o caminho que deve ser seguido para encontrar o atributo
   * @throws IllegalAccessException
   * @throws InvocationTargetException 
   */
  @Override
  public void set(Object from, Object val, int index, String... paths)
          throws IllegalAccessException, InvocationTargetException{
    if( index > -1 && paths.length == 1 ){
      paths = paths[0].split("\\.");
    }
    if( index < 0 ) index *= -1;
    if( index < paths.length-1 ){
      Object novoFrom = get(from, -1, paths[index]);
      path(0).set(novoFrom, val, index +1, paths);
    }else{
      if( setter != null ) setter.invoke(from, val);
    }
  }
  /**
   * Uma forma simples de invocar o método <code>add(Object)</code>
   * 
   * @param that o objeto onde a reflexão será aplicada
   * @param params os valores que serão aplicados na reflexão
   * @throws IllegalAccessException
   * @throws InvocationTargetException 
   */
  public void add(Object that, Object... params)
          throws IllegalAccessException, InvocationTargetException{
    try{
      Collection col = (Collection)getter.invoke(that);
      if( col == null ){
        if( Set.class.equals( colecaoType ) ) col = new HashSet<>();
        else if( List.class.equals( colecaoType )
          || Collection.class.equals( colecaoType ) )
            col = new ArrayList<>();
        else col = (Collection)colecaoType.newInstance();
        setter.invoke(that, col);
      }
      for( Object param : params ) col.add( param );
    }catch(Exception ex){
      throw new InvocationTargetException(ex);
    }
  }
  /**
   * Uma forma simples de invocar o método <code>remove(Object)</code>
   * 
   * @param that o objeto onde a reflexão será aplicada
   * @param params os valores que serão aplicados na reflexão
   * @throws IllegalAccessException
   * @throws InvocationTargetException 
   */
  public void remove(Object that, Object... params)
          throws IllegalAccessException, InvocationTargetException{
    if( remove != null ) 
      for( Object param : params )
        remove.invoke(that, param);
  }

  
}
