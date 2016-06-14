
package br.eng.rcc.framework.seguranca.servicos;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.persistencia.MsgException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import br.eng.rcc.framework.interfaces.IUsuario;
import br.eng.rcc.framework.persistencia.JsonResponse;
import br.eng.rcc.framework.seguranca.entidades.ChaveAcesso;
import br.eng.rcc.framework.seguranca.entidades.Credencial;
import br.eng.rcc.framework.seguranca.entidades.Grupo;
import br.eng.rcc.framework.seguranca.entidades.SegUsuario;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.servlet.http.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Esta classe fornece funções para acessar o usuário que está disponível na
 * sessão atual do servidor (em relação a requisição que está correndo).
 * 
 * 
 */
@Specializes
@ApplicationScoped
public class UsuarioServicoImpl extends UsuarioServico{
  
  private static Logger log = LogManager.getLogger();
  
  @Inject
  protected EntityManager em;
  
  //=======================  Utilitarios  =========================
  
  @Override
  public byte[] criptografar(String str){
    if( !Configuracoes.getInstance().criptografiaAtivo() ) return str.getBytes();
    
    byte[] bytes = str.getBytes();
    char[] chars = new char[ str.length() ];
    str.getChars(0, str.length(), chars, 0);
    
    String criptoName = Configuracoes.getInstance().criptografia();
    byte[] salt = Configuracoes.getInstance().criptografiaSalt().getBytes();
    int iteration = Configuracoes.getInstance().criptografiaIteration();
    int keyLength = Configuracoes.getInstance().criptografiaKeyLength();
    
    try{
      SecretKeyFactory skf = SecretKeyFactory.getInstance( criptoName );
      KeySpec scs = new PBEKeySpec( chars, salt, iteration, keyLength );
      return skf.generateSecret(scs).getEncoded();
    }catch(NoSuchAlgorithmException | InvalidKeySpecException ex1){
      try{
        Mac mac = Mac.getInstance( criptoName );
        mac.init( new SecretKeySpec( bytes, criptoName ) );
        return mac.doFinal( bytes );
      }catch(NoSuchAlgorithmException | InvalidKeyException ex2){
        try{
          MessageDigest mDigest = MessageDigest.getInstance( criptoName );
          return mDigest.digest( bytes );
        }catch(NoSuchAlgorithmException ex3){
          log.warn("Não encontramos uma Impl. do script de criptografia: {}", criptoName);
        }
      }
    }
    //return DigestUtils.getDigest( Configuracoes.encriptionScript ).digest( str.getBytes() ); // ApacheCodec
    return new byte[0];
  }
  
  
  @Override
  public boolean checkLogin(boolean returnOrThrow){
    IUsuario usuario = getUsuario();
    if( usuario != null ) return true;
    
    if( this.req.getCookies() != null ) for( Cookie cookie : this.req.getCookies() ){
      if( !cookie.getName().equals( Configuracoes.getInstance().loginCookieName() ) )continue;
      List<ChaveAcesso> oo = em.createQuery("SELECT x FROM ChaveAcesso x WHERE x.chave = :chave")
              .setParameter("chave", cookie.getValue())
              .setMaxResults(1)
              .getResultList();
      if( oo.size() > 0 ){
        this.req.getSession().setAttribute( IUsuario.USUARIO_KEY , oo.get(0).getCredencial().getUsuario().clone() );
        return true;
      }
    }
    
    if( !returnOrThrow ) throw new MsgException(JsonResponse.ERROR_DESLOGADO,null,"O usuário não está logado");
    return false;
  }
  
  
  @Override
  public boolean checkLogin(){
    return checkLogin(false);
  }
  
  
  @Override
  public SegUsuario getUsuario(){
    SegUsuario u = (SegUsuario)super.getUsuario();
    if( u == null ){
      if( this.req.getCookies() != null ) for( Cookie cookie : this.req.getCookies() ){
        if( !cookie.getName().equals( Configuracoes.getInstance().loginCookieName() ) )continue;
        List<ChaveAcesso> oo = em.createQuery("SELECT x FROM ChaveAcesso x WHERE x.chave = :chave")
                .setParameter("chave", cookie.getValue())
                .setMaxResults(1)
                .getResultList();
        if( oo.size() > 0 ){
          Credencial c = oo.get(0).getCredencial();
          // temos que carregar os atrasados!:
          c.getPermissoes().size();
          if( c.getGrupos() != null ) for(Grupo g : c.getGrupos()) g.getPermissoes().size();
          
          u = c.getUsuario().clone();
          this.req.getSession().setAttribute( IUsuario.USUARIO_KEY , u );
        }
      }
    }
    return u;
  }
  
}
