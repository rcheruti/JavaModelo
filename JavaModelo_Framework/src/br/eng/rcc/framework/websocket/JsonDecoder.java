
package br.eng.rcc.framework.websocket;

import br.eng.rcc.framework.jaxrs.JsonRequestReader;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.StringReader;
import javax.inject.Inject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class JsonDecoder implements Decoder.Text {
    
    @Inject
    private JsonRequestReader reader;
    
    @Override
    public Object decode(String s) throws DecodeException {
        try{
            return reader.fromJson( new StringReader(s), JsonNode.class );
        }catch(IOException ex){
            
        }
        return null;
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig config) {
        
    }

    @Override
    public void destroy() {
    }

}
