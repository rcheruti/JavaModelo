
package br.eng.rcc.framework.websocket;

import br.eng.rcc.framework.persistencia.JsonResponse;
import javax.websocket.RemoteEndpoint;


public class Status {
    
    private RemoteEndpoint.Async remote ;
    
    
    public void setRemote(RemoteEndpoint.Async remote){ this.remote = remote; }
    
    
    public void send(JsonResponse obj){
        if( this.remote == null ) return;
    }
    
}
