
package br.eng.rcc.framework.seguranca.anotacoes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Use essa anotação para ativar o interceptador CDI de segurança em um método.
 * 
 * @author rcheruti
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@InterceptorBinding
public @interface SegurancaMetodo {
  
  
  @Nonbinding
  String value() default "";
  
  @Nonbinding
  String grupo() default "";
  
}
