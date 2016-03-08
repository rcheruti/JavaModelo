
package br.eng.rcc.framework.config;

import br.eng.rcc.framework.jaxrs.JacksonObjectMapperContextResolver;
import br.eng.rcc.framework.seguranca.config.SegurancaRootNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;

/**
 * Essa classe faz a leitura dos arquivos de configuraçao de segurança, que 
 * pode ser escrito em <strong>JSON</strong> ou <i>XML (este ainda está pendente!!!)</i>
 * 
 * @author Rafael
 */
public class SegurancaConfig {
    
  private static SegurancaRootNode segurancas;
  
  public static void init() throws IOException{
    System.out.printf("------  Iniciando leitura de segurança: \n");

    URL url = SegurancaConfig.class.getClassLoader().getResource("META-INF/seguranca.json");
    if( url == null ) url = SegurancaConfig.class.getClassLoader().getResource("seguranca.json");
    System.out.printf("------ Endereço: %s \n", url );

    if( url != null ){
      ObjectMapper mapper = new JacksonObjectMapperContextResolver().getContext(null);
      segurancas = mapper.readValue(url, SegurancaRootNode.class);
    }

  }
  
  public static SegurancaRootNode getSegurancas(){ return segurancas; }
  
}
