
package br.eng.rcc.framework.websocket;

import java.util.Arrays;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

@WebListener
public class Instalador implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        
        try{
            ServerContainer ws = (ServerContainer) ctx
                    .getAttribute("javax.websocket.server.ServerContainer");
            if( ws == null ) return;
            
            String urlParam = ctx.getInitParameter("websocket.url");
            if( urlParam == null ) urlParam = "/websocket";
            ws.addEndpoint( ServerEndpointConfig.Builder
                .create(Socket.class, urlParam )
                .configurator( new Configurador() )
                .decoders( Arrays.asList( JsonDecoder.class ) )
                .encoders( Arrays.asList( JsonEncoder.class ) )
                .build()
            );
        }catch(DeploymentException | NullPointerException ex){
            
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }

}
