
package br.eng.rcc.framework.seguranca.anotacoes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.interceptor.InterceptorBinding;

/**
 * Esta anotação é usada para informar uma lista de {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca} 
 * que deverão ser verificadas pelo serviço de segurança.
 * 
 * Essas anotações serão verificadas usando a operação lógica "OU": caso o usuário
 * atual tenha uma das seguranças informadas, ele terá permissão para acessar aquele recurso.
 * 
 * Esta anotação será usada pelo {@link br.eng.rcc.framework.jaxrs.persistence.EntidadesService EntidadesService} para
 * que seja possível informar nome de permissões ou nome de grupos diferentes para cada modo
 * que a permissão tem de restrição.
 * Ex.: grupo1 pode atualizar, mas não criar novos. grupo2 pode ler, mas não pode atualizar nem criar novos.
 * 
 * @author rcheruti
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@InterceptorBinding
public @interface Segurancas {
    
    Seguranca[] value() default {};
    
}
