
package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/utils/*")
public class ServletUtils extends HttpServlet{
  
  @Inject
  private UsuarioServico uServ;
  
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException{
    
    PrintWriter w = resp.getWriter();
    String[] paths = req.getPathInfo().split("/");
    if( paths.length < 1 ) paths = new String[]{""};
    switch(paths[1]){
      case "senha":
        w.append( new String(uServ.criptografar( paths[2] )) );
        break;
      default:
        w.append( String.format("Escreva uma URL como: '.../utils/senha/123MinhaSenha. Enviado: '%s'", req.getPathInfo()) );
    }
    
    w.flush();
  }
  
}
