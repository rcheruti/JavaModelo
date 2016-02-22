
package br.eng.rcc.framework.seguranca.config;

import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import java.util.List;
import java.util.Map;

/**
 * Essa classe é uma entidade de dados usada para guardar as informações
 * que serão lidas dos arquivos de configuração de segunrança.
 * 
 * Um arquivo de configuração JSON pode ser escrito como:
<pre>
  {
    "enderecos":{
       "enderecoRegex":{ Objeto Seguranca.class e suas propriedades }
    },
    "entidades":{
       "EntidadeNomeJPA":{ Objeto Seguranca.class e suas propriedades }
    }
  }
</pre>
 * 
 * Uma chave no objeto "enderecos" é uma Expressão Regular que será usada
 * para verificar se uma determinada URL receberá essas restrições 
 * de segurança.
 * 
 * @author Rafael
 */
public class SegurancaRootNode {
  
  protected Map<String,List<SegurancaNode>> enderecos;
  
  protected Map<String,List<SegurancaNode>> entidades;
  
  
  public void setEnderecos(Map<String,List<SegurancaNode>> x){ this.enderecos = x; }
  public Map<String,List<SegurancaNode>> getEnderecos(){ return this.enderecos; }
  
  public void setEntidades(Map<String,List<SegurancaNode>> x){ this.entidades = x; }
  public Map<String,List<SegurancaNode>> getEntidades(){ return this.entidades; }
  
}
