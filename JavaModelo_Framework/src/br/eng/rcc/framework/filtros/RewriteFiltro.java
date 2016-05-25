package br.eng.rcc.framework.filtros;

import br.eng.rcc.framework.config.Configuracoes;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RewriteFiltro implements Filter {

  private Pattern regexp;

  @Override
  public void init(FilterConfig fc) throws ServletException {
    regexp = Pattern.compile(Configuracoes.getInstance().rewriteRegExp());
  }

  @Override
  public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc)
          throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) sr;
    String uri = req.getRequestURI();
    String urictx = req.getContextPath();
    if (urictx != null && urictx.length() > 0) {
      uri = uri.substring(urictx.length());
    }

    HttpServletResponse resp = (HttpServletResponse) sr1;
    resp.addHeader("cache-control", "no-cache");

    String header = req.getHeader("X-Requested-With");
    if (
          "OPTIONS".equals( req.getMethod().toUpperCase() ) 
          || "XMLHttpRequest".equals(header)
          || regexp.matcher(uri).find()) {
      fc.doFilter(sr, sr1);
    } else {
      //req.getRequestDispatcher(Configuracoes.indexPath).forward(sr, sr1);
      resp.sendRedirect( req.getContextPath()+Configuracoes.getInstance().indexPath() );
    }
  }

  @Override
  public void destroy() {
  }

}
