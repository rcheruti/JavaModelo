
package br.eng.rcc.framework.config;

import br.eng.rcc.framework.filtros.RewriteFiltro;
import br.eng.rcc.framework.seguranca.filtros.SegurancaFiltro;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;

/**
 * As configurações do sistema poderão ser carregadas a partir de um arquivo 
 * localizado em "/META-INF/"
 * 
 * @author rcheruti
 */
public class Configuracoes {
  
  // constantes do sistema
  public static final String JSON_PERSISTENCIA = "application/json";
  
  /*
    Todas as propriedades/atributos colocados nesta classe que forem "public static" serão
    carregados automaticamente dos arquivos de configuração. Não é necessário manutenção
    em nenhuma outra parte do sistema para ter as configurações disponíveis.
  */
  
  /**
   * Nome do Cookie que será inserido na resposta do Login no sistema, esse tem a ChaveAcesso
   * para ser feito o login por cookie (ex.: quando o servidor for reiniciado, o navegador e o banco
   * ainda se "lembram" dessa chave e é possível recarregar algumas informações da sessão do usuário)
   * 
   * JMCL: JavaModelo Login Cookie
   */
  public static String loginCookieName = "JMLC";
  
  /**
   * Expressão Regular que será usada por {@link RewriteFiltro} para a reescrita de URLs.
   * Caso alguma página que seja validada por esse expressão seja acessa sem que a requisição
   * tenha o cabeçalho de AJAX ("X-Requested-With: XMLHttpRequest"), o filtro irá redirecionar
   * o usuário para a página configurada em {@link indexPath}.
   */
  public static String rewriteRegExp = 
      "^/?s/|^/?css/|^/?js/|^/?img/|^/?index\\.html|^/?login\\.html|^/?index\\.jsp|^/?login\\.jsp" ;
  
  /**
   * Expressão Regular que será usada por {@link SegurancaFiltro} proteger o sistema de tentativas
   * de acesso sem login.
   * Caso a URL que está sendo acessada seja validada por esta expressão e o usuário ainda
   * não estiver logado, o Filtro irá enviar o usuário para o endereço configurado em 
   * {@link loginPath}.
   */
  public static String segurancaRegExp = 
      "(?i:^/?(?:img|css|js|s/seguranca/login|login.html|login.jsp))" ;
  
  
  /**
   * Endereço da página de login do sistema.
   * (Atenção: não incluir o Contexto do sistema nesse valor)
   */
  public static String loginPath = "/login.jsp";
  
  /**
   * Endereço da página inicial do sistema, após o login ser efetuado.
   * (Atenção: não incluir o Contexto do sistema nesse valor)
   */
  public static String indexPath = "/index.jsp";
  
  /**
   * Nome do script de criptografia que será passado para o "Apache Codec Commons" para fazer a 
   * criptografia das senhas dos usuários no serviço {@link UsuarioServico}
   * 
   * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html">
   *  Está página
   * </a> contém a lista dos scripts padrão da JVM (de implementação obrigatória para as JVMs). 
   * 
   */
  public static String criptografia = "PBKDF2WithHmacSHA1";
  
  /**
   * SALT para ser usado junto com a senha do usuário no momento que for criada a
   * criptografia da senha.
   */
  public static String criptografiaSalt = "Persistencia";
  
  /**
   * Quantidade de iterações da chave quando uma chave de "SecretKey" for usavada.
   * <br/><br/>
   * Ex.: "PBKDF2WithHmacSHA1"
   */
  public static int criptografiaIteration = 5;
  
  /**
   * Tamanho da chave quando uma chave de "SecretKey" for usavada.
   * <br/><br/>
   * Ex.: "PBKDF2WithHmacSHA1"
   */
  public static int criptografiaKeyLength = 512;
  
  /**
   * Informa se é necessário fazer a criptografia das senhas dos usuários.
   * (Atenção: apenas configure esta opção durante testes!)
   */
  public static boolean criptografiaAtivo = false;
  
  
  
  /**
   * Informa qual o limite de entidades que podem ser retornadas nas requisições
   * de Entidades_Service.
   * 
   * Esse limite é usado por proteção, para não buscar informações demais do 
   * banco em uma única requisição.
   */
  public static int limiteEntidadesSize = 100;
  /**
   * Informa qual é o tamanho padrão de uma busca (cláusula LIMIT do banco de dados).
   */
  public static int sizeEntidadeDefault = 30;
  /**
   * Informa qual a página inicial padrão de uma busca (para encontrar a 
   * cláusula OFFSET do banco de dados).
   */
  public static int pageEntidadeDefault = 0;
  
  // configurações do filtro de CORS
  
  /**
   * Informa se o cache de classes, carregado usando o {@link Metamodel} do
   * {@link EntityManager}, deve ser preenchido logo que o sistema iniciar.
   */
  public static boolean preCarregarCache = false;
  
}
