package br.eng.rcc.framework.seguranca.anotacoes;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.seguranca.predicados.NullSupplier;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Esta é a anotação de segurança.
 *
 * Esta anotação funciona de duas formas:
 * <ul>
 * <li>
 * Quando uma Entidade JPA é anotada, esta anotação será verifica pelo
 * {@link br.eng.rcc.framework.jaxrs.persistence.EntidadesService EntidadesService}
 * para informar se o usuário atual tem as permissões necessárias para executar
 * a tarefa solicitada para esta entidade (SALVAR, LER, EDITAR, DELETAR).
 *
 * </li>
 * <li>
 * Quando está anotação é anotada em um recurso da CDI, um interceptador esta
 * preparado para verificar se o usuário tem as permissções necessárias para
 * acessar aquele método (ou métodos).
 * </li>
 * </ul>
 *
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@InterceptorBinding
@Repeatable(Segurancas.class)
public @interface Seguranca {

  /** Máscara binária do SELECT, valor = 1 */
  public static final int SELECT = 1;
  /** Máscara binária do INSERT, valor = 2 */
  public static final int INSERT = 2;
  /** Máscara binária do UPDATE, valor = 4 */
  public static final int UPDATE = 4;
  /** Máscara binária do DELETE, valor = 8 */
  public static final int DELETE = 8;
  
  public static final String emptyString = "";

  /**
   * Este atributo guarda o nome da chave de segurança
   * ({@link br.eng.rcc.framework.seguranca.entidades.Permissao Permissao}) que
   * o usuário logado atualmente deve ter para ter acesso a este recurso.
   *
   * Quando é anotado um recurso CDI, o interceptador bloqueará a execução do
   * método caso o usuário não tenha a permissão.
   *
   *
   * Quando uma entidade JPA é anotada, o
   * {@link br.eng.rcc.framework.jaxrs.persistence.EntidadesService EntidadesService}
   * verificará se o usuário tem a permissão necessária para acessar o recurso,
   * considerando a "ação" que foi solicitada (SELECT, INSERT, UPDATE, DELETE)
   *
   * @return {@link java.lang.String String}
   */
  @Nonbinding
  String value() default "";

  /**
   * Este atributo guarda o nome do grupo que tem permissão de acessar este
   * recurso.
   *
   * @return {@link java.lang.String String}
   */
  @Nonbinding
  String grupo() default "";
  
  
  /** Indica se o usuário atual está autorizado a fazer SELECT */
  @Nonbinding
  boolean select() default true;
  
  /** Indica se o usuário atual está autorizado a fazer INERT */
  @Nonbinding
  boolean insert() default true;
  
  /** Indica se o usuário atual está autorizado a fazer UPDATE */
  @Nonbinding
  boolean update() default true;
  
  /** Indica se o usuário atual está autorizado a fazer DELETE */
  @Nonbinding
  boolean delete() default true;

  /**
   * Esse método é usado para testar se o usuário atual (a requisição atual) tem
   * permissão para acessar este dado.
   *
   * Com este método pode-se criar formas mais customizadas de testes de acesso,
   * podendo testar a permissão de acesso de acordo com um dado desse tipo (ao
   * invés de fazer o teste em relação ao tipo/modelo).
   *
   * @return {@link Class<? extends Supplier<Predicate>>} O predicado que será
   * usado para testar o acesso
   */
  @Nonbinding
  @Deprecated
  Class<? extends Supplier<? extends Predicate>> predicado() default NullSupplier.class;
  
  
  @Nonbinding
  Class<? extends SegurancaPersistenciaInterceptador>[] filters() default { } ;
  
}
