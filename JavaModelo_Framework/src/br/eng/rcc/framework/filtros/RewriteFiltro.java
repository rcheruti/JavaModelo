
package br.eng.rcc.framework.filtros;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public class RewriteFiltro implements Filter{
    
    private Pattern regexp;
    private static final String defaultRegexp = 
            "^/?s/|^/?css/|^/?js/|^/?img/|^/?index\\.html|^/?login\\.html|^/?index\\.jsp|^/?login\\.jsp";
    
    @Override
    public void init(FilterConfig fc) throws ServletException {
        String regexpStr = fc.getInitParameter("regexp");
        if( regexpStr == null ) regexpStr = defaultRegexp;
        regexp = Pattern.compile(regexpStr);
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) 
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)sr;
        String uri = req.getRequestURI();
        String urictx = req.getContextPath();
        if( urictx != null && urictx.length() > 0 ){
            uri = uri.substring( urictx.length() );
        }
        
        HttpServletResponse resp = (HttpServletResponse)sr1;
        resp.addHeader("cache-control", "no-cache");
        
        String header = req.getHeader("X-Requested-With");
        if( "XMLHttpRequest".equals(header)
                || regexp.matcher(uri).find() ){
            fc.doFilter(sr, sr1);
        }else{
            req.getRequestDispatcher("/index.jsp").forward(sr, sr1);
        }
    }

    @Override
    public void destroy() {
    }

}
