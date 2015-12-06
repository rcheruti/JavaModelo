
package br.eng.rcc.framework.websocket;

import br.eng.rcc.framework.jaxrs.JsonResponseWriter;
import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class JsonEncoder implements Encoder.Text{
    
    
    @Inject
    private JsonResponseWriter writer;
    
    @Override
    public String encode(Object object) throws EncodeException {
        return writer.toJson(object);
    }

    @Override
    public void init(EndpointConfig config) {
        
    }

    @Override
    public void destroy() {
        
    }

}
