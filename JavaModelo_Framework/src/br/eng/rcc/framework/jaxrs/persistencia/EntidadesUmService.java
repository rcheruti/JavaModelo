package br.eng.rcc.framework.jaxrs.persistencia;

import br.eng.rcc.framework.config.Configuracoes;
import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.utils.PersistenciaUtils;
import br.eng.rcc.framework.seguranca.servicos.SegurancaServico;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/persistencia/um/{entidade}")
@Produces({Configuracoes.JSON_PERSISTENCIA})
@Consumes({Configuracoes.JSON_PERSISTENCIA})
@RequestScoped
public class EntidadesUmService {

  @Inject
  protected EntityManager em;
  @Inject
  protected ClassCache cache;
  @Inject
  protected SegurancaServico checker;
  @Inject
  protected EntidadesService entService;

  /**
   * Para que este objeto possa fazer o seu trabalho, é obrigatório um
   * {@link EntityManager EntityManager} para acessar o banco.
   */
  @PostConstruct
  public void postConstruct() {
    if (em == null) {
      String msg = "O objeto EM é nulo! Verifique as configurações do Banco.";
      Logger.getLogger(this.getClass().getName()).log(Level.WARNING, msg);
      throw new MsgException(JsonResponse.ERROR_DESCONHECIDO,null,msg);
    }
  }

  //=============================================================================
  //=============================================================================
  //=============================================================================
  /*
        Abaixo estão os métodos que trabalharão com apenas 1 tipo de entidade por vez
   */
  @POST
  @Path("/tipo")
  public JsonResponse tipo(
          @PathParam("entidade") String entidade
  ) {
    Class<?> klass = cache.get(entidade, em);
    if (klass == null) {
      return new JsonResponse(false, String.format("Não encontramos nenhuma entidade para '%s'", entidade));
    }
    return new JsonResponse(true, entService.tipo(klass), "Tipo um");
  }

  /**
   * Exemplo de URL para abusca:
   * "http://www.endereco.com.br/jaxRootContext/persistence/MinhaClasse;matrix1=valor;matrix2=valor?attr1=valor
   * & attr2 != valor2"
   *
   * Ex.: ".../persistence/Comprador;join=carros;order=nome;page=3;size=30?
   * idade > 18" Retornará as entidades da classe "Comprador" que tenham o
   * atributo "idade" (a classe DEVE ter este atributo) maior que 18, ordenados
   * pelo "nome", sendo que a busca retornará a página 4 (pois "page" inicia em
   * 0) limitada em 30 itens.
   *
   * Parâmetros da matrix: page: Este parâmetro informa a página que deverá ser
   * retornada (cláusula OFFSET), iniciando em 0. size: Este parâmetro informa a
   * quantidade de itens que retornarão por página (cláusula LIMIT). join: Este
   * parâmetro informa quais atributos devemos incluir no "FETCH JOIN"
   * (cláusulas JOIN). order: Este parâmetro informa quais atributos serão
   * usados para a ordenação (cláusula ORDER BY).
   *
   * @param entidade Nome simples da classe que iremos buscar uma entidade.
   * @param ctx Este parâmetro será informado pelo JAX-RS, daqui pegaremos a
   * "Query String" que será usada para fazer a filtragem das entidades
   * (cláusula WHERE).
   * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
   */
  @POST
  @Path("/buscar")
  public JsonResponse buscar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx
  ) {
    String path = String.format("%s?%s", ctx.getPath(), ctx.getRequestUri().getQuery() );
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca(path);
    return new JsonResponse(true, entService.buscar(info), "Buscar um", 
            info.page, info.size);
  }

  /**
   * Este método deve receber no "PathParam" o nome simples da classe que iremos
   * criar uma nova entidade no banco de dados.
   *
   * Os dados dessa entidade devem ser enviados no corpo do POST, no formato
   * JSON. Atributos que não estejam presentes receberão o seu valor padrão
   * (valor padrão da instrução "new"), e atributos que não pertençam a esta
   * entidade serão ignorados.
   *
   * - Para gravar relações "OneToOne" é necessário adicionar "cascade =
   * PERSIST/ALL". - Para gravar relações "ManyToMany" não é permitido adicionar
   * "cascade = PERSIST/ALL" na relação. - Para gravar relações "OneToMany" é
   * necessário adicionar a todos os itens do outro lado da relação (que terá
   * uma coleção por ser "ManyToOne") uma referência a este objeto. Isso pode
   * ser feito no método GET. Necessário adicionar "cascade = PERSIST/ALL". - -
   * Atenção: caso esteja usando Lombok a persistência pode dispara um loop
   * infinito por causa dos métodos "toString", "hascode" ou "equals". Caso veja
   * "StackOverflowError" no console verifique esses métodos.
   *
   *
   * @param objs Lista de objetos para persistência no banco de dados
   * @param entidade Nome da entidade de banco de dados
   * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
   */
  @POST
  @Path("/")
  @Transactional
  public JsonResponse criar(List<?> objs, @PathParam("entidade") String entidade) {
    if (objs == null || objs.isEmpty()) {
      return new JsonResponse(false,
              String.format("Não encontramos nenhuma entidade para '%s'", entidade));
    }
    entService.criar(objs, entidade);
    
    return new JsonResponse(true, null, "Criar um");
  }

  /**
   * Este método deve receber no "PathParam" o nome simples da classe que iremos
   * editar uma entidade no banco de dados.
   *
   * Na "Query String" deve ser informado os atributos de filtragem que devemos
   * usar para encontrar esta entidade na base. A "Query String" <strong>é
   * obrigatória!</strong>
   * <i>Não é permitido atualizar todos os registros do banco de dados, mas é
   * possível atualizar vários.</i>
   *
   * Os dados que serão atualizados devem ser passados no corpo do PUT. Não é
   * permitido informar atributos que não pertençam a esta entidade, caso isso
   * aconteça um erro de atualização (UPDATE) acontecerá e nada será atualizado.
   *
   * @param entidade
   * @param ctx
   * @param obj
   * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
   */
  @PUT
  @Path("/")
  @Transactional
  public JsonResponse editar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx,
          JsonNode obj) { // JsonNode
    String uriQuery = ctx.getRequestUri().getQuery();
    if (uriQuery == null || uriQuery.isEmpty()) {
      return new JsonResponse(false,
              "Para editar registros é necessário informar os parâmetros de filtragem na QueryString.");
    }
    String path = String.format("%s?%s", ctx.getPath(), uriQuery );
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( path, cache);
    if( obj.isArray() ) obj = obj.get(0);
    
    int ups = entService.editar(info, obj);
    return new JsonResponse(true, ups, "Editar um");
  }

  /**
   * Este método deve receber no "PathParam" o nome simples da classe que iremos
   * deletar uma entidade no banco de dados.
   *
   * Na "Query String" deve ser informado os atributos de filtragem que devemos
   * usar para encontrar esta entidade na base. A "Query String" <strong>é
   * obrigatória!</strong>
   * <i>Não é permitido deletar todos os registros do banco de dados, mas é
   * possível deletar vários.</i>
   *
   *
   * @param entidade
   * @param ctx
   * @return {@link br.eng.rcc.framework.jaxrs.JsonResponse JsonResponse}
   */
  @DELETE
  @Path("/")
  @Transactional
  public JsonResponse deletar(
          @PathParam("entidade") String entidade,
          @Context UriInfo ctx
  ) {
    String uriQuery = ctx.getRequestUri().getQuery();
    if (uriQuery == null || uriQuery.isEmpty()) {
      return new JsonResponse(false,
              "Para apagar registros é necessário informar os parâmetros de filtragem na QueryString.");
    }
    String path = String.format("%s?%s", ctx.getPath(), uriQuery );
    PersistenciaUtils.BuscaInfo info = PersistenciaUtils.parseBusca( path );
    
    int qtd = entService.deletar(info);
    return new JsonResponse(true, qtd, "Deletar um");
  }
  
  
  
  
}
