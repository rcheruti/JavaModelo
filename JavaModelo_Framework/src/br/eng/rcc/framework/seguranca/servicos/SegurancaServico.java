
package br.eng.rcc.framework.seguranca.servicos;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.anotacoes.Segurancas;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import br.eng.rcc.framework.interfaces.IUsuario;


/**
 * Recursos desta classe estarão disponpiveis na CDI.
 * 
 * Este recurso é o responsável por verificar se o usuário atual tem as permissões
 * necessárias para executar ou acessar algum recurso.
 * 
 * As outras partes deste Framework que verificam segurança em algum momento usam
 * esta classe para essa verificação. Esta é a classe que pode ser usada caso
 * seja necessário fazer uma verificação de segurança no código.
 * 
 * @author rcheruti
 */
@ApplicationScoped
public class SegurancaServico { 
    
    @Inject
    protected HttpServletRequest req;
    
    
    /**
     * Este método irá executar {@link br.eng.rcc.seguranca.servicos.SegurancaServico#check(java.lang.String, java.lang.String) .check( valor, null ) }
     * 
     * @param valor Nome da permissão que será checada.
     */
    public void check(String valor){
        check(valor, null);
    }
    
    /**
     * Este método verifica se o usuário tem a permissão informada ou se 
     * pertence ao grupo informado.
     * 
     * @param valor Nome da permissão que será checada. 
     * @param grupoK Nome do grupo que será checado.
     */
    public void check(String valor, String grupoK){
        if( valor == null && grupoK == null ){
            throw new MsgException("Uma chave de segurança não pode ser nula!");
        }
        
        try{
            IUsuario usuario = (IUsuario) req.getAttribute(IUsuario.USUARIO_KEY );
            if( usuario == null ){
                throw new MsgException("Não existe usuário logado");
            }
            if( valor != null && !Seguranca.emptyString.equals(valor) ){
                if( usuario.hasPermissao(valor) ) return;
            }
            if( grupoK != null && !Seguranca.emptyString.equals(grupoK) ){
                if( usuario.hasGrupo(grupoK) ) return;
            }
            throw new MsgException("O usuário não tem permissão para acessar este recurso");
        }catch(ClassCastException ex){
            throw new MsgException( JsonResponse.ERROR_DESLOGADO, null, "Não existe usuário logado");
        }
    }
    
    
    /**
     * Este método irá procurar na classe informada a anotação {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca}
     * para verificar se o usuário atual tem esta permissão.
     * 
     * Será permitido todos o modos de segurança (SELECT, INSERT, UPDATE e DELETE).
     * 
     * @param klass Classe em que será pesquisado se possúi anotação {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca}.
     */
    public void check(Class<?> klass){
        this.check(klass, Seguranca.SELECT | Seguranca.INSERT | 
                Seguranca.UPDATE | Seguranca.DELETE );
    }
    
    /**
     * Este método irá procurar na classe informada a anotação {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca}
     * para verificar se o usuário atual tem esta permissão.
     * 
     * Será permitido apenas o modo de segurança informado (SELECT, INSERT, UPDATE ou DELETE).
     * 
     * @param klass Classe em que será pesquisado se possúi anotação {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca}.
     * @param mode Uma das constantes da classe {@link Seguranca}
     */
    public void check(Class<?> klass, int mode){
        Seguranca ann = klass.getAnnotation(Seguranca.class);
        Segurancas annS = klass.getAnnotation(Segurancas.class);
        
        Seguranca[] lista;
        if( ann != null ){
            lista = new Seguranca[1];
            lista[0] = ann ;
        }else if( annS != null ){
            lista = annS.value() ;
        }else{
            return ;
        }
        
        for( Seguranca s : lista ){
            String valor = s.value();
            String grupo = s.grupo();
            
            try{
                if( (mode & Seguranca.SELECT) != 0 && s.select() ){
                    this.check(valor,grupo);
                    return ;
                }else if( (mode & Seguranca.INSERT) != 0 && s.insert() ){
                    this.check(valor,grupo);
                    return ;
                }else if( (mode & Seguranca.UPDATE) != 0 && s.update() ){
                    this.check(valor,grupo);
                    return ;
                }else if( (mode & Seguranca.DELETE) != 0 && s.delete() ){
                    this.check(valor,grupo);
                    return ;
                }
            }catch(MsgException ex){
                if( ex.getCodigo() == JsonResponse.ERROR_DESLOGADO ) throw ex;
            }
        }
        
        throw new MsgException("O usuário não tem permissão para acessar este recurso");
    }
    
    
    
    
}
