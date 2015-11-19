
package br.eng.rcc.seguranca.servicos;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.seguranca.anotacoes.Seguranca;
import br.eng.rcc.seguranca.anotacoes.Segurancas;
import br.eng.rcc.seguranca.entidades.Usuario;
import br.eng.rcc.seguranca.filtros.SegurancaFiltro;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;


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
    public static final int SELECT = 1;
    public static final int INSERT = 2;
    public static final int UPDATE = 4;
    public static final int DELETE = 8;
    private static final String nullString = "";
    
    
    @Inject
    private HttpServletRequest req;
    
    
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
            Usuario usuario = (Usuario) req.getAttribute( SegurancaFiltro.usuarioKey );
            if( usuario == null ){
                throw new MsgException("Não existe usuário logado");
            }
            if( valor != null && !nullString.equals(valor) ){
                if( usuario.getCredencial().hasPermissao(valor) ) return;
            }
            if( grupoK != null && !nullString.equals(grupoK) ){
                if( usuario.getCredencial().hasGrupo(grupoK) ) return;
            }
            throw new MsgException("O usuário não tem permissão para acessar este recurso");
        }catch(ClassCastException ex){
            throw new MsgException( JsonResponse.Error_LoggedOut, null, "Não existe usuário logado");
        }
    }
    
    
    /**
     * Este método irá procurar na classe informada a anotação {@link br.eng.rcc.seguranca.anotacoes.Seguranca Seguranca}
     * para verificar se o usuário atual tem esta permissão.
     * 
     * Será permitido todos o modos de segurança (SELECT, INSERT, UPDATE e DELETE).
     * 
     * @param klass Classe em que será pesquisado se possúi anotação {@link br.eng.rcc.seguranca.anotacoes.Seguranca Seguranca}.
     */
    public void check(Class<?> klass){
        this.check(klass, SELECT | INSERT | UPDATE | DELETE );
    }
    
    /**
     * Este método irá procurar na classe informada a anotação {@link br.eng.rcc.seguranca.anotacoes.Seguranca Seguranca}
     * para verificar se o usuário atual tem esta permissão.
     * 
     * Será permitido apenas o modo de segurança informado (SELECT, INSERT, UPDATE ou DELETE).
     * 
     * @param klass Classe em que será pesquisado se possúi anotação {@link br.eng.rcc.seguranca.anotacoes.Seguranca Seguranca}.
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
                if( (mode & SELECT) != 0 && s.select() ){
                    this.check(valor,grupo);
                    return ;
                }else if( (mode & INSERT) != 0 && s.insert() ){
                    this.check(valor,grupo);
                    return ;
                }else if( (mode & UPDATE) != 0 && s.update() ){
                    this.check(valor,grupo);
                    return ;
                }else if( (mode & DELETE) != 0 && s.delete() ){
                    this.check(valor,grupo);
                    return ;
                }
            }catch(MsgException ex){
                if( ex.getCodigo() == JsonResponse.Error_LoggedOut ) throw ex;
            }
        }
        
        throw new MsgException("O usuário não tem permissão para acessar este recurso");
    }
    
    
    
    
}
