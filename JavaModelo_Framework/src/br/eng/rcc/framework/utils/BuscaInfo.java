package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.config.Configuracoes;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Esta classe é usada para guadar as informações de uma requisição
 * ao servidor.
 * <br><br>
 * Aqui estarão as informações necessárias para as outras partes do framework
 * decidam que caminho seguir no decorrer do script.
 * 
 */
public class BuscaInfo implements Cloneable{
  
  // Constantes de busca:
  public static final byte ACAO_BUSCAR =       1;
  public static final byte ACAO_CRIAR =        2;
  public static final byte ACAO_EDITAR =       3;
  public static final byte ACAO_DELETAR =      4;
  public static final byte ACAO_TIPO =         5;
  public static final byte ACAO_ADICIONAR =    6;
  public static final byte ACAO_REMOVER =      7;
  public static final byte ACAO_PAGINAR =      10;
  
  //========================================================================
  
  /**
   * Indica qual é a página atual.
   */
  public int page = Configuracoes.getInstance().pageEntidadeDefault();
  /**
   * Indica a quantidade de itens que devem ser carregados 
   * para a página atual, no máximo.
   */
  public int size = Configuracoes.getInstance().sizeEntidadeDefault();
  /**
   * A lista dos atributos que devem ser carregados para a resposta, que
   * provavelmente são <b>"Lazy"</b>.
   */
  public List<String> join;
  /**
   * A lista de Strings que serão usadas para fazer a ordenação.
   * <br><br>
   * Pode estar nos formatos: <b>"attr"</b>, <b>"attr ASC"</b> ou <b>"attr
   * DESC"</b>
   */
  public List<String> order;
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
  public List<String[]> where;
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
  
  /**
   * Este método irá criar um novo objeto {@link BuscaInfo}, copiando todos os atributos
   * do objeto original para que não haja ligação (referência) nenhuma entre o
   * novo objeto e o antigo objeto.
   * <br><br>
   * A cópia dos atributos é feita dessa maneira:
   * <ul>
   *  <li>
   *    Atributos nativos são copiados para o novo objeto (não existe referências aqui,
   *    então nenhum problema acontecerá)
   *  </li>
   *  <li>
   *    Atributos que são vetores de nativos ou {@link String} são copiados 
   *    usando {@link Arrays.copyOf} (objetos {@link String} não permitem que o vetor de 
   *    caracteres sejá alterado, então não haverá problemas aqui)
   *  </li>
   *  <li>
   *    Para o atributo <b>where</b> é criado um novo vetor, e para cada item do vetor
   *    no antigo objeto (cada item também é um vetor) é usado {@link Arrays.copyOf}
   *    para copiar o vetor de {@link String}.
   *  </li>
   * </ul>
   * 
   * @return Um novo objeto {@link BuscaInfo}
   */
  @Override
  public BuscaInfo clone() {
    //BuscaInfo x = (BuscaInfo)super.clone();
    BuscaInfo x = new BuscaInfo();
    x.acao = this.acao;
    x.classe = this.classe;
    x.size = this.size;
    x.page = this.page;
    x.entidade = this.entidade;
    x.id = this.id;
    x.join = new ArrayList<>(this.join);
    x.order = new ArrayList<>(this.order);
    x.where = new ArrayList<>( this.where.size()+1 );
    for( int i = 0; i < this.where.size(); i++ )
      x.where.add( Arrays.copyOf( this.where.get(i) , this.where.get(i).length ) );
    x.data = this.data.deepCopy();
    return x;
  }
  
}
