
package br.eng.rcc.framework.interfaces;

import br.eng.rcc.framework.utils.Busca;
import java.util.List;

/**
 * Essa interface é usada para permitir que o programador
 * tenha uma chance de adicionar restrições as buscas do banco.
 * 
 * Esses métodos serão executados após ... fazer as configurações
 * iniciais da busca, qualquer alteração tem que ser feita com cuidado para não
 * quebrar as configurações que já foram adicionadas em {@link Busca}.
 * 
 * @param <T> O tipo da classe esperado (será desconsiderado durante a execução!)
 */
public interface SegurancaPersistenciaInterceptador<T> {
  
  default void filter(Busca busca){};
  
  default List<T> filter(List<T> objs){ return objs; };
    
}
