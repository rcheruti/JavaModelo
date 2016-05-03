
package br.eng.rcc.framework.filtros;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSFiltro implements Filter{

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, 
          FilterChain chain) throws IOException, ServletException {
    HttpServletResponse resp = (HttpServletResponse) response;
    HttpServletRequest req = (HttpServletRequest) request;
    
    resp.setHeader("Access-Control-Allow-Origin", "*");
    if( req.getHeader("Origin") != null ){
      resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
    }
    resp.setHeader("Access-Control-Allow-Headers", "origin,content-type,accept,authorization,authorization,x-requested-with,cookies,*");
    resp.setHeader("Access-Control-Allow-Credentials", "true");
    resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    resp.setHeader("Access-Control-Max-Age", "12000");
    resp.setHeader("Cache-Control", "no-cache");
    chain.doFilter(request, response);
  }

  @Override
  public void destroy(){
    
  }
    
}
