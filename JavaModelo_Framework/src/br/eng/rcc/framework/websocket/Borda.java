
package br.eng.rcc.framework.websocket;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;



public class Borda extends Endpoint{
    
    @OnOpen
    @Override
    public void onOpen(Session session, EndpointConfig config){
        Status st = (Status) config.getUserProperties().get( Status.class.getName() );
        if( st != null ){
            RemoteEndpoint.Async remote = session.getAsyncRemote();
            st.setRemote(remote);
        }
    }
    
    @OnClose
    @Override
    public void onClose(Session session, CloseReason closeReason){
        super.onClose(session, closeReason);
    }
    
    @OnMessage
    public void onMessage(){
        
    }
    
    @OnError
    @Override
    public void onError(Session session, Throwable thr){
        super.onError(session, thr);
    }
    
}
