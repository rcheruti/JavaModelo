
package br.eng.rcc.framework.seguranca.anotacoes;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Esta é a anotação de segurança.
 * 
 * Esta anotação funciona de duas formas:
 * <ul>
 * <li>
 * Quando uma Entidade JPA é anotada, esta anotação será verifica pelo {@link br.eng.rcc.framework.jaxrs.persistence.EntidadesService EntidadesService}
 * para informar se o usuário atual tem as permissões necessárias para executar a tarefa solicitada para esta entidade (SALVAR, LER, EDITAR, DELETAR).
 * 
 * </li>
 * <li>
 * Quando está anotação é anotada em um recurso da CDI, um interceptador esta preparado para verificar se o usuário tem as permissções
 * necessárias para acessar aquele método (ou métodos).
 * </li>
 * </ul>
 * 
 * 
 * @author rcheruti
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@InterceptorBinding
public @interface Seguranca {
    
    public static final int SELECT = 1;
    public static final int INSERT = 2;
    public static final int UPDATE = 4;
    public static final int DELETE = 8;
    public static final String emptyString = "";
    
    /**
     * Este atributo guarda o nome da chave de segurança ({@link br.eng.rcc.framework.seguranca.entidades.Permissao Permissao})
     * que o usuário logado atualmente deve ter para ter acesso a este recurso.
     * 
     * Quando é anotado um recurso CDI, o interceptador bloqueará a execução do método caso
     * o usuário não tenha a permissão.
     * 
     * 
     * Quando uma entidade JPA é anotada, o {@link br.eng.rcc.framework.jaxrs.persistence.EntidadesService EntidadesService} 
     * verificará se o usuário tem a permissão necessária para acessar o recurso, considerando a "ação" que foi solicitada (SELECT, INSERT, UPDATE, DELETE)
     * 
     * @return {@link java.lang.String String}
     */
    @Nonbinding String value() default "";
    
    /**
     * Este atributo guarda o nome do grupo que tem permissão de acessar este recurso.
     * 
     * 
     * 
     * @return {@link java.lang.String String}
     */
    @Nonbinding String grupo() default "";
    
    @Nonbinding boolean select() default true;
    @Nonbinding boolean insert() default true;
    @Nonbinding boolean update() default true;
    @Nonbinding boolean delete() default true;
    
}
