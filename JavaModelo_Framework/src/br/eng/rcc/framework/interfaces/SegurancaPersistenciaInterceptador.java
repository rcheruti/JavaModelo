
package br.eng.rcc.framework.interfaces;

import br.eng.rcc.framework.jaxrs.persistencia.EntidadesService;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;

/**
 * Essa interface é usada por {@link EntidadesService} para permitir que o programador
 * tenha uma chance de adicionar restrições as buscas do banco.
 * 
 * Esses métodos serão executados após {@link EntidadesService} fazer as configurações
 * iniciais da busca, qualquer alteração tem que ser feita com cuidado para não
 * quebrar as configurações que já foram adicionadas por {@link EntidadesService}.
 * 
 * @param <T> O tipo da classe esperado (será desconsiderado durante a execução!)
 */
public interface SegurancaPersistenciaInterceptador<T> {
  
  /**
   * Permite a configuração das buscas (SELECT) em {@link EntidadesService}.
   * @param cb
   * @param query 
   */
  default void check(CriteriaBuilder cb, CriteriaQuery query){};
  /**
   * Permite a configuração das atualizações (UPDATE) em {@link EntidadesService}.
   * @param cb
   * @param query 
   */
  default void check(CriteriaBuilder cb, CriteriaUpdate query){};
  /**
   * Permite a configuração das exclusões (DELETE) em {@link EntidadesService}.
   * @param cb
   * @param query 
   */
  default void check(CriteriaBuilder cb, CriteriaDelete query){};
  /**
   * Permite a configuração das inserções (INSERT) em {@link EntidadesService}.
   * @param obj 
   */
  default void check(T obj){};
    
}
