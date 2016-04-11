
package br.eng.rcc.framework.seguranca.servicos;

import br.eng.rcc.framework.jaxrs.MsgException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import br.eng.rcc.framework.interfaces.IUsuario;
import javax.enterprise.context.RequestScoped;

/**
 * Esta classe fornece funções para acessar o usuário que está disponível na
 * sessão atual do servidor (em relação a requisição que está correndo).
 * 
 * 
 */
@RequestScoped
public class UsuarioServico {
  
  @Inject
  protected HttpServletRequest req;
  
  
  //=======================  Utilitarios  =========================
  
  public IUsuario getUsuario(){
    HttpSession session = req.getSession(false);
    if( session != null ){
      try{
        IUsuario u = (IUsuario) session.getAttribute(IUsuario.USUARIO_KEY);
        return u;
      }catch(ClassCastException ex){}
    }
    return null;
  }
  
  /**
   * Essa implementação apenas retorna o vetor de bytes da String.
   * Sobreescreva este método para criptografar as senhas.
   * 
   * @param str
   * @return 
   */
  public byte[] criptografar(String str){
    return str.getBytes();
  }
  
  
  /**
   * Verifica se existe um usuário logado atualmente, tanto verificando a sessão atual ou verificando
   * o cookie enviado pelo cliente.
   * 
   * Sobreescreva este método para alterar o processo de verificação.
   * 
   * @param returnOrThrow FALSE para disparar uma {@link MsgException} caso o usuário não esteja
   *    logado, TRUE para retornar o valor da consulta como um `boolean`.
   * @return Retorna TRUE caso o usuário esteja logado, FALSE caso não esteja, 
   *    ou dispara uma {@link MsgException} caso o parâmetros `returnOrThrow` seja FALSE e o 
   *    usuário não esteja logado.
   */
  public boolean checkLogin(boolean returnOrThrow){
    IUsuario usuario = getUsuario();
    if( usuario != null ) return true;
    
    if( !returnOrThrow ) throw new MsgException("O usuário não está logado");
    return false;
  }
  
  /**
   * Este método é o mesmo que executar `checkLogin(FALSE)`.
   * Verifique a documentação de {@link checkLogin(boolean)} para mais informações.
   * Sobreescreva {@link checkLogin(boolean)} para alterar o processo de verificação.
   * 
   * @return Retorna o valor de {@link checkLogin(boolean)}.
   */
  public boolean checkLogin(){
    return checkLogin(false);
  }
  
  
  
}
