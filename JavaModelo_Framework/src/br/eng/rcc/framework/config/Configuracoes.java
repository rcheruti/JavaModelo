
package br.eng.rcc.framework.config;

import br.eng.rcc.framework.filtros.RewriteFiltro;
import br.eng.rcc.framework.seguranca.filtros.SegurancaFiltro;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;

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
      "(?i:^/?img|^/?css|^/?js|^/?s/seguranca/login|^/?login.html|^/?login.jsp)" ;
  
  
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
  public static String encriptionScript = "PBKDF2WithHmacSHA1";
  
  /**
   * Informa se é necessário fazer a criptografia das senhas dos usuários.
   * (Atenção: apenas configure esta opção durante testes!)
   */
  public static boolean encriptionActive = true;
  
  
  // configurações do filtro de CORS
  
  
}
