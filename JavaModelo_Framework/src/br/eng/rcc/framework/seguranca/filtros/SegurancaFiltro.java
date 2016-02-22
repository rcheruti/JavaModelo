
package br.eng.rcc.framework.seguranca.filtros;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.config.SegurancaConfig;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.JsonResponseWriter;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.config.SegurancaNode;
import br.eng.rcc.framework.seguranca.config.SegurancaRootNode;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.MediaType;

public class SegurancaFiltro implements Filter{
  
  
  private Pattern pattern;
  private String redirectPage;
  private Map<String,UrlSeguranca> urlCache = new HashMap<>( 20 );
  private Map<String,Boolean> urlCacheBoolean = new HashMap<>( 60 );
  
  @Inject
  private UsuarioServico usuarioService;
  
  @Inject
  private SegurancaServico segurancaServico;
  

  @Inject
  private JsonResponseWriter writer;

  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    pattern = Pattern.compile( Configuracoes.segurancaRegExp );
    redirectPage = Configuracoes.loginPath;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, 
      FilterChain chain) throws IOException, ServletException {
    
    HttpServletRequest req = (HttpServletRequest) request;
    
    String ctxPath = req.getContextPath();
    String uri = req.getRequestURI().substring( ctxPath.length() );
    System.out.printf("-----  Request URI: %s \n", uri );
    
    Matcher matcher = pattern.matcher(uri);
    
    // Checar se essa é uma URL pública
    if( matcher.find() ){
      chain.doFilter(request, response);
      return;
    }
    
    try{
      usuarioService.checkLogin();
      
      Boolean bool = urlCacheBoolean.get(uri);
      
      System.out.printf("---  bool: %s \n", bool );
      if( bool == null ){
        SegurancaRootNode node = SegurancaConfig.getSegurancas();
        Map<String,List<SegurancaNode>> map = node.getEnderecos();
        UrlSeguranca seg = new UrlSeguranca();
        for( String regex : map.keySet() ){
          Pattern patternSeg = Pattern.compile( regex );
          Matcher matcherSeg = patternSeg.matcher( uri );
          if( matcherSeg.find() ){
            bool = true;
            for( Seguranca s : map.get(regex) ) seg.getList().add(s);
          }
        }
        if( bool == null ) bool = false;
        if( bool ) urlCache.put( uri, seg);
        urlCacheBoolean.put(uri, bool);
      }
      
      if( bool ){
        segurancaServico.check( urlCache.get(uri) );
      }
      
    }catch(MsgException ex){
      HttpServletResponse resp = (HttpServletResponse) response;
      resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      resp.addHeader("Pragma", "no-cache");
      resp.addHeader("Expires", "0"); 
      JsonResponse res = new JsonResponse( false, ex.getCodigo(), ex.getData(), ex.getMensagem() );
      writer.writeTo(res, res.getClass(), null, 
                        new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, 
                        response.getOutputStream() );
      return;
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
