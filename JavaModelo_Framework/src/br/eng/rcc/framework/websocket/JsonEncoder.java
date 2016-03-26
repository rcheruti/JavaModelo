
package br.eng.rcc.framework.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class JsonEncoder implements Encoder.Text{
    
    
    
    @Override
    public String encode(Object object) throws EncodeException {
        return null;
    }

    @Override
    public void init(EndpointConfig config) {
        
    }

    @Override
    public void destroy() {
        
    }

}
