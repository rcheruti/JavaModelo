
package br.eng.rcc.framework.seguranca.filtros;

import br.eng.rcc.framework.seguranca.servicos.UsuarioService;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SegurancaFiltro implements Filter{
  
  private final static String defaultPattern = 
      "(?i:^/?img|^/?css|^/?js|^/?s/seguranca/login|^/?login.html|^/?login.jsp)";
  private final static String defaultRedirectPage = "/login.jsp";
  
  //------------------------------------------------------------
  
  private Pattern pattern;
  private String redirectPage;
  
  @Inject
  private UsuarioService usuarioService;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String patternStr = filterConfig.getServletContext()
        .getInitParameter("SegurancaFiltro.pattern");
    if( patternStr == null ){
      patternStr = defaultPattern;
    }
    pattern = Pattern.compile(patternStr);
      //  ---
    
    String redirectPageStr = filterConfig.getServletContext()
        .getInitParameter("SegurancaFiltro.redirectPage");
    redirectPage = (redirectPageStr == null)? defaultRedirectPage : redirectPageStr;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, 
      FilterChain chain) throws IOException, ServletException {
    
    HttpServletRequest req = (HttpServletRequest) request;
    
    String ctxPath = req.getContextPath();
    String uri = req.getRequestURI().substring( ctxPath.length() );
    
    Matcher matcher = pattern.matcher(uri);
    
    // Checar se essa é uma URL pública
    if( matcher.find() ){
      chain.doFilter(request, response);
      return;
    }
    
    try{
      usuarioService.checkLogin();
    }catch(Exception ex){
      HttpServletResponse resp = (HttpServletResponse) response;
      resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      resp.addHeader("Pragma", "no-cache");
      resp.addHeader("Expires", "0");
      req.getRequestDispatcher( redirectPage ).forward(request, response);
      return; // Sair, pois nao podemos deixar processar a requisição
    }
    
    // Tudo ok aqui
    chain.doFilter(request, response);
  }

  @Override
  public void destroy(){
    pattern = null;
  }

}
