
package br.eng.rcc.javamodelo.test.seguranca;

import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.seguranca.entidades.Grupo;
import br.eng.rcc.framework.seguranca.entidades.Permissao;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.javamodelo.test.entidades.Usuario;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SerUsuarioLogadoFilter implements SegurancaPersistenciaInterceptador<Usuario>{
  
  @Inject
  private UsuarioServico uServ;
  
  @Override
  public void filter(BuscaInfo busca) {
    //System.out.printf("---  Seguranca:SerUsuarioLogadoFilter:filter(BuscaInfo) \n");
    Usuario u = (Usuario)uServ.getUsuario();
    busca.where.add( new String[]{"registroUsuario.usuario.id","=", ""+u.getId() ,"&"} );
    
  }

  @Override
  public List<Usuario> filter(List<Usuario> objs) {
    //System.out.printf("---  Seguranca:SerUsuarioLogadoFilter:filter(List<Usuario>) \n");
    return objs;
  }
  
  
  
}
