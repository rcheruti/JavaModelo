package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.config.Configuracoes;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Esta classe é usada para guadar as informações de uma requisição
 * ao servidor.
 * <br><br>
 * Aqui estarão as informações necessárias para as outras partes do framework
 * decidam que caminho seguir no decorrer do script.
 * 
 */
public class Busca implements Cloneable{
  
  // Constantes de busca:
  public static final byte ACAO_BUSCAR =       1;
  public static final byte ACAO_CRIAR =        2;
  public static final byte ACAO_EDITAR =       3;
  public static final byte ACAO_DELETAR =      4;
  public static final byte ACAO_TIPO =         5;
  public static final byte ACAO_ADICIONAR =    6;
  public static final byte ACAO_REMOVER =      7;
  public static final byte ACAO_PAGINAR =      10;
  
  public static final byte ACAO_REFS =         15;
  
  //========================================================================
  
  /**
   * Indica qual a identificação dessa busca na lista de referências.
   * 
   * 
   */
  public String id;
  
  /**
   * Esse atributo irá guardar (se necessário) a estrutura de objetos que aparecem
   * em mais de um lugar na resposta/requisição.
   * 
   * Quando as referências aparecem dentro do objeto de busca elas são válidas
   * só para esse objeto de busca.
   * 
   * Quando aparecem como um objeto de busca, elas são válidas para todas as outras
   * buscas que acompanham a resposta/requisição.
   * 
   * ! Atenção: como a lista de requisição/resposta é um vetor, e a ordem de execução
   * é garantida, o objeto que for configurado como REFS só será interpretado
   * por buscas que vierem depois da declaração da busca com as referências!
   */
  public Map<String, Object> refs;
  
  
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
  public String from;
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
  public boolean chaves = false;
  /**
   * Indica qual ação será aplicada a from.
   */
  public byte acao = ACAO_BUSCAR;
  
  //=========================================================================
  
  //public CommonAbstractCriteria criteria;
  //public Query criteriaQuery;
  //public List resultado;
  
  /**
   * Este método irá criar um novo objeto {@link Busca}, copiando todos os atributos
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
   * @return Um novo objeto {@link Busca}
   */
  @Override
  public Busca clone() {
    //BuscaInfo x = (Busca)super.clone();
    Busca x = new Busca();
    x.id = this.id;
    x.acao = this.acao;
    x.classe = this.classe;
    x.size = this.size;
    x.page = this.page;
    x.from = this.from;
    x.chaves = this.chaves;
    x.join = new ArrayList<>(this.join);
    x.order = new ArrayList<>(this.order);
    x.where = new ArrayList<>( this.where.size()+1 );
    for( int i = 0; i < this.where.size(); i++ )
      x.where.add( Arrays.copyOf( this.where.get(i) , this.where.get(i).length ) );
    x.data = this.data.deepCopy();
    return x;
  }
  
}
