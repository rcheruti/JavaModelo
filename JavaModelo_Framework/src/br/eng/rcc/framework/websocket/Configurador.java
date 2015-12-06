
package br.eng.rcc.framework.websocket;

import javax.inject.Inject;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class Configurador extends ServerEndpointConfig.Configurator {
    
    @Inject
    private Status status;
    
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, 
            HandshakeRequest request, HandshakeResponse response){
        super.modifyHandshake(sec,request,response);
        sec.getUserProperties().put( Status.class.getName() , status);
    }
    
}
