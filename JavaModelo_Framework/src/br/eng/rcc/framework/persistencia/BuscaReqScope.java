
package br.eng.rcc.framework.persistencia;

import java.util.HashMap;
import java.util.Map;

public class BuscaReqScope {
  
  /**
   * Referências de objetos usam a chave <b>@</b>.
   * <br><br>
   * Aqui será guardado as referências de objetos para as busca.
   */
  public Map<String,Object> refs = new HashMap<>(16);
  
  /**
   * Referências de respostas usam a chave <b>#</b>.
   * <br><br>
   * Aqui será guardado as referências resposta para as busca.
   */
  public Map<String,Object> resps = new HashMap<>(16);
  
}
