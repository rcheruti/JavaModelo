package br.eng.rcc.framework.seguranca.servicos;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.seguranca.anotacoes.Seguranca;
import br.eng.rcc.framework.seguranca.anotacoes.Segurancas;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import br.eng.rcc.framework.interfaces.IUsuario;
import br.eng.rcc.framework.interfaces.SegurancaPersistenciaInterceptador;
import br.eng.rcc.framework.utils.BuscaInfo;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Recursos desta classe estarão disponpiveis na CDI.
 *
 * Este recurso é o responsável por verificar se o usuário atual tem as
 * permissões necessárias para executar ou acessar algum recurso.
 *
 * As outras partes deste Framework que verificam segurança em algum momento
 * usam esta classe para essa verificação. Esta é a classe que pode ser usada
 * caso seja necessário fazer uma verificação de segurança no código.
 *
 * @author rcheruti
 */
@ApplicationScoped
public class SegurancaServico {
  
  @Inject
  protected UsuarioServico uServico;

  protected Map<Class, Supplier> suppliers = new HashMap<>(30);
  protected Map<AnnotatedElement, Seguranca[]> segurancas = new HashMap<>(50);
  protected Map<AnnotatedElement, List<SegurancaPersistenciaInterceptador>> checkPersistencias = new HashMap<>(50);
  
  //=============================================================================
  

  /**
   * Este método irá executar {@link br.eng.rcc.seguranca.servicos.SegurancaServico#check(java.lang.String, java.lang.String) .check( valor, null )
   * }
   *
   * @param valor Nome da permissão que será checada.
   */
  public void check(String valor) {
    check(valor, null);
  }

  /**
   * Este método verifica se o usuário tem a permissão informada ou se pertence
   * ao grupo informado.
   *
   * @param valor Nome da permissão que será checada.
   * @param grupoK Nome do grupo que será checado.
   */
  public void check(String valor, String grupoK) {
    if (  (valor == null || Seguranca.emptyString.equals(valor)) && 
          (grupoK == null || Seguranca.emptyString.equals(grupoK))
        ){
      return;
      //throw new MsgException(JsonResponse.ERROR_PERMISSAO, null, "Uma chave de segurança não pode ser nula!");
    }

    try {
      IUsuario usuario = uServico.getUsuario();
      if (usuario == null) {
        throw new MsgException(JsonResponse.ERROR_DESLOGADO, null, "Não existe usuário logado");
      }
      boolean temPermissao = 
        (valor == null || Seguranca.emptyString.equals(valor) || usuario.hasPermissao(valor)) &&
        (grupoK == null || Seguranca.emptyString.equals(grupoK) || usuario.hasGrupo(grupoK));
      
      if( temPermissao )return;
      
      throw new MsgException(JsonResponse.ERROR_PERMISSAO, null, "O usuário não tem permissão para acessar este recurso");
    } catch (ClassCastException ex) {
      throw new MsgException(JsonResponse.ERROR_DESLOGADO, null, "Não existe usuário logado");
    }
  }
  
  
  
  
  
  /**
   * Este método irá procurar na classe informada a anotação
   * {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca} para
   * verificar se o usuário atual tem esta permissão.
   *
   * Será permitido todos o modos de segurança (SELECT, INSERT, UPDATE e
   * DELETE).
   *
   * @param annotated Elemento em que será pesquisado se possúi anotação
   * {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca}.
   */
  public void check(AnnotatedElement annotated) {
    this.check(annotated, 
            Seguranca.SELECT 
            | Seguranca.INSERT
            | Seguranca.UPDATE 
            | Seguranca.DELETE);
  }

  /**
   * Este método irá procurar na classe informada a anotação
   * {@link br.eng.rcc.framework.seguranca.anotacoes.Seguranca Seguranca} para
   * verificar se o usuário atual tem esta permissão.
   *
   * Será permitido apenas o modo de segurança informado (SELECT, INSERT, UPDATE
   * ou DELETE).
   *
   * @param annotated Elemento em que será pesquisado se possúi anotação {@link Seguranca Seguranca}.
   * @param mode Uma das constantes da classe {@link Seguranca Seguranca}
   */
  public void check(AnnotatedElement annotated, int mode) {
    Seguranca[] lista = getSegurancas(annotated);
    if( lista == null ) return;
    
    for (Seguranca s : lista) {
      try {
        if(
          ( (mode & Seguranca.SELECT) != 0 && s.select() ) ||
          ( (mode & Seguranca.INSERT) != 0 && s.insert() ) ||
          ( (mode & Seguranca.UPDATE) != 0 && s.update() ) ||
          ( (mode & Seguranca.DELETE) != 0 && s.delete() )
            ){
          this.check( s.value(), s.grupo() );
          return;
        }
      } catch (MsgException ex) {
        if (ex.getCodigo() == JsonResponse.ERROR_DESLOGADO) {
          throw ex;
        }
      }
    }

    throw new MsgException(JsonResponse.ERROR_PERMISSAO, null, "O usuário não tem permissão para acessar este recurso");
  }
  
  
  
  
  
  public void check(AnnotatedElement annotated, Object obj){
    Object[] objs = { obj };
    check( annotated, objs );
  }
  public void check(AnnotatedElement annotated, Object[] objs){
    check( annotated );
    checkPredicado( annotated, objs );
  }
  public void checkPredicado(AnnotatedElement annotated, Object obj){
    Object[] objs = { obj };
    checkPredicado( annotated, objs );
  }
  /**
   * Esse método irá usar os {@link Predicate Predicate} configurados nas anotações
   * de segurança para testar se o usuário atual tem permissão de acesso aos dados,
   * quando a anotação for usada em uma entidade do banco.
   * 
   * @param annotated Elemento com as anotações de segurança
   * @param objs Dados que serão testados
   */
  public void checkPredicado(AnnotatedElement annotated, Object[] objs) {
    Seguranca[] lista = getSegurancas(annotated);
    if( lista == null ) return;
    
    for (Seguranca s : lista) {
      Class supClass = s.predicado();
      if( supClass == null ) continue;
      Supplier<Predicate> sup = suppliers.get( supClass );
      if( sup == null ){
        try{
          sup = (Supplier) supClass.newInstance();
          suppliers.put(supClass, sup);
        }catch(InstantiationException | IllegalAccessException ex){
          throw new MsgException( JsonResponse.ERROR_DESCONHECIDO, null, ex.getMessage(), ex );
        }
      }
      Predicate pred = sup.get();
      if( pred == null ) continue;
      for(Object obj : objs){
        if( !pred.test(obj) ) throw new MsgException(JsonResponse.ERROR_PERMISSAO, null, "O usuário não tem permissão para acessar este recurso");
      }
    }
    
  }
  
  
  //===========================================================================
  
  
  public void filterPersistencia(BuscaInfo busca){
    for( SegurancaPersistenciaInterceptador x : _getCheckPersistencia( busca.classe ) ){
      x.filter( busca );
    }
  }
  public <T> List<T> filterPersistencia(BuscaInfo busca, List<T> objs){
    for( SegurancaPersistenciaInterceptador x : _getCheckPersistencia( busca.classe ) ){
      objs = x.filter( objs );
    }
    return objs;
  }
  private List<SegurancaPersistenciaInterceptador> _getCheckPersistencia(AnnotatedElement annotated){
    List<SegurancaPersistenciaInterceptador> spi = checkPersistencias.get(annotated);
    if( spi == null ){
      spi = new ArrayList<>();
      checkPersistencias.put( annotated , spi);
      Seguranca[] lista = getSegurancas(annotated);
      if( lista != null ){
        for( Seguranca s : lista ){
          for( Class<? extends SegurancaPersistenciaInterceptador> spiX : s.filters() ){
            try{
              spi.add( spiX.newInstance() );
            }catch(InstantiationException | IllegalAccessException ex){
              throw new MsgException(JsonResponse.ERROR_DESCONHECIDO, ex.getMessage(), ex);
            }
          }
        }
      }
    }
    return spi;
  }
  
  
  //=========================================================================
  protected Seguranca[] getSegurancas(AnnotatedElement annotated) {
    Seguranca[] lista = segurancas.get(annotated);
    if( lista != null ) return lista;
    
    Seguranca ann = annotated.getAnnotation(Seguranca.class);
    Segurancas annS = annotated.getAnnotation(Segurancas.class);
    
    if (ann != null) {
      lista = new Seguranca[1];
      lista[0] = ann;
    } else if (annS != null) {
      lista = annS.value();
    }
    if( lista != null ) segurancas.put(annotated, lista);
    return lista;
  }

}
