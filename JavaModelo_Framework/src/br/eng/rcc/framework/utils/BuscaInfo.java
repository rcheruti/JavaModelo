package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.config.Configuracoes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Esta classe é usada para guadar as informações de uma requisição
 * ao servidor.
 * <br><br>
 * Aqui estarão as informações necessárias para as outras partes do framework
 * decidam que caminho seguir no decorrer do script.
 * 
 */
public class BuscaInfo {
  
  // Constantes de busca:
  public static final byte ACAO_BUSCAR =       1;
  public static final byte ACAO_CRIAR =        2;
  public static final byte ACAO_EDITAR =       3;
  public static final byte ACAO_DELETAR =      4;
  public static final byte ACAO_TIPO =         5;
  public static final byte ACAO_ADICIONAR =    6;
  public static final byte ACAO_REMOVER =      7;
  
  //========================================================================
  
  /**
   * Indica qual é a página atual.
   */
  public int page = Configuracoes.pageEntidadeDefault;
  /**
   * Indica a quantidade de itens que devem ser carregados 
   * para a página atual, no máximo.
   */
  public int size = Configuracoes.sizeEntidadeDefault;
  /**
   * A lista dos atributos que devem ser carregados para a resposta, que
   * provavelmente são <b>"Lazy"</b>.
   */
  public String[] join;
  /**
   * A lista de Strings que serão usadas para fazer a ordenação.
   * <br><br>
   * Pode estar nos formatos: <b>"attr"</b>, <b>"attr ASC"</b> ou <b>"attr
   * DESC"</b>
   */
  public String[] order;
  /**
   * Query String dessa busca, já interpretada.
   * <br><br>
   * Esse vetor está no formato:
   * <pre>
   * [
   *   [ "Nome do atributo",
   *     "comparador do banco",
   *     "valor do atributo",
   *     "operação E ou Ou"
   *   ]
   * ]
   * </pre>
   */
  public String[][] query;
  /**
   * Nome simples da classe dessa entidade, como a JPA mapeia as entidades.
   */
  public String entidade;
  /**
   * Classe da entidade.
   */
  public Class<?> classe;
  /**
   * Valor que vem no corpo da mensagem HTTP.
   */
  public ArrayNode data;
  /**
   * Indica se o modelo de script que esta busca esta pedindo é de ID.
   * <br><br>
   * Esta busca irá sempre representar o modelo UM, e este campo indica se o
   * modelo UM é de ID ou não.
   */
  public boolean id = false;
  /**
   * Indica qual ação será aplicada a entidade.
   */
  public byte acao = ACAO_BUSCAR;
  
  
  //=========================================================================
  
  //public CommonAbstractCriteria criteria;
  //public Query criteriaQuery;
  //public List resultado;
  
  @Override
  public BuscaInfo clone(){
    BuscaInfo x = new BuscaInfo();
    x.acao = this.acao;
    x.classe = this.classe;
    x.data = this.data;
    x.entidade = this.entidade;
    x.id = this.id;
    x.join = this.join;
    x.order = this.order;
    x.page = this.page;
    x.query = this.query;
    x.size = this.size;
    return x;
  }
  
}
