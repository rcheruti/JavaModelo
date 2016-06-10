
package br.eng.rcc.framework.config;

import br.eng.rcc.framework.filtros.RewriteFiltro;
import br.eng.rcc.framework.persistencia.EntidadesService;
import br.eng.rcc.framework.seguranca.filtros.SegurancaFiltro;
import br.eng.rcc.framework.seguranca.servicos.UsuarioServico;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * As configurações do sistema poderão ser carregadas a partir de um arquivo 
 * localizado em "/META-INF/"
 * 
 * @author rcheruti
 */
public class Configuracoes {
  
  private static Logger log = LogManager.getLogger(Configuracoes.class);
  
  private Configuracoes(){
    configs.put( Key.JSON_PERSISTENCIA.name(), "application/json");
    configs.put( Key.loginCookieName.name(), "JMLC");
    configs.put( Key.rewriteRegExp.name(), 
      "(?i:^/?(s/|css/|js/|img/|persistencia/context|exportar|utils|.*\\.js$|.*\\.css$|$))");
    configs.put( Key.segurancaRegExp.name(), 
      "(?i:^/?(?:img/|css/|js/|seguranca/login|persistencia/context|utils|.*\\.js$|.*\\.css$))");
    configs.put( Key.loginPath.name(), "/login.html");
    configs.put( Key.indexPath.name(), "/");
    configs.put( Key.criptografia.name(), "PBKDF2WithHmacSHA1");
    configs.put( Key.criptografiaSalt.name(), "Persistencia");
    configs.put( Key.criptografiaIteration.name(), 100);
    configs.put( Key.criptografiaKeyLength.name(), 512);
    configs.put( Key.criptografiaAtivo.name(), true);
    configs.put( Key.limiteEntidadesSize.name(), 200);
    configs.put( Key.sizeEntidadeDefault.name(), 50);
    configs.put( Key.pageEntidadeDefault.name(), 0);
    configs.put( Key.preCarregarCache.name(), false);
    configs.put( Key.persistenceUnit.name(), "");
    configs.put( Key.carregarDB.name(), null);
    configs.put( Key.hibernate.name(), new HashMap<>(30));
    configs.put( Key.hibernateAutoLoad.name(), true);
    configs.put( Key.entidadesClasses.name(), new HashSet<>(50));
    configs.put( Key.configDir.name(), System.getProperty("user.home"));
    
    // Carregar padrões:
    Map<String,Object> hibernate = hibernate();
    hibernate.put("hibernate.c3p0.min_size", "5");
    hibernate.put("hibernate.c3p0.max_size", "30");
    hibernate.put("hibernate.c3p0.timeout", "300");
    hibernate.put("hibernate.c3p0.max_statements", "50");
    hibernate.put("hibernate.c3p0.idle_test_period", "3000");
    hibernate.put("hibernate.archive.autodetection", "hbm,class");
  }
  
  protected Map<String,Object> configs = new HashMap<>(40);
  
  
  //===============   Statics   ==================
  
  private static Configuracoes instance = null;
  private static boolean carregado;
  private static String nomeArq = "persistencia.json";
  
  public static Configuracoes getInstance(){
    if( instance == null ){
      instance = new Configuracoes();
    }
    return instance;
  }
  
  public static void carregar(){
    if(carregado) return;
    getInstance(); // Criar instância (se precisar)
    try{
      File file = new File(getInstance().configDir(), nomeArq);
      log.info("Carregando arquivo de configuracao em: {}", file.getAbsolutePath());
      Map<String,Object> mapLoad = arqMapper().readValue(
        Files.newReader(file, Charset.forName("UTF-8")) , Map.class);
      if( mapLoad != null && mapLoad.size() > 0 ){
        getInstance().configs = mapLoad;
      }else{
        log.warn("Não foi encontrado nenhuma informacao no arquivo de configuracao, carregaremos os padroes.");
        PersistenciaConfig.init();
      }
    }catch(IOException ex){
      log.warn("Não foi encontrado nenhuma informacao no arquivo de configuracao, carregaremos os padroes.");
      try{
        PersistenciaConfig.init();
      }catch(IOException ex2){
        log.error("Problemas ao tentar carregar as configuracoes!");
        throw new RuntimeException("------ Problemas ao tentar carregar as configuracoes!", ex);
      }
    }
    carregado = true;
    salvar();
  }
  public static void salvar(){
    try{
      File file = new File(getInstance().configDir(), nomeArq);
      log.info("Gravando arquivo de configuracao em: {}", file.getAbsolutePath());
      arqMapper().writeValue(
        Files.newWriter(file, Charset.forName("UTF-8")) , getInstance().configs);
    }catch(IOException ex){
      log.error("Não foi possível gravar as configurações neste computador!");
    }
  }
  private static ObjectMapper arqMapper(){
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
    
    mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
    
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
    mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    mapper.disable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
    
    return mapper;
  }
  
  
  
  
  
  
  public static enum Key{
    JSON_PERSISTENCIA,
    
  /**
   * Nome do Cookie que será inserido na resposta do Login no sistema, esse tem a ChaveAcesso
   * para ser feito o login por cookie (ex.: quando o servidor for reiniciado, o navegador e o banco
   * ainda se "lembram" dessa chave e é possível recarregar algumas informações da sessão do usuário)
   * 
   * JMCL: JavaModelo Login Cookie
   */
    loginCookieName,
    
  /**
   * Expressão Regular que será usada por {@link RewriteFiltro} para a reescrita de URLs.
   * Caso alguma página que seja validada por esse expressão seja acessa sem que a requisição
   * tenha o cabeçalho de AJAX ("X-Requested-With: XMLHttpRequest"), o filtro irá redirecionar
   * o usuário para a página configurada em {@link indexPath}.
   */
    rewriteRegExp,
    
  /**
   * Expressão Regular que será usada por {@link SegurancaFiltro} proteger o sistema de tentativas
   * de acesso sem login.
   * Caso a URL que está sendo acessada seja validada por esta expressão e o usuário ainda
   * não estiver logado, o Filtro irá enviar o usuário para o endereço configurado em 
   * {@link loginPath}.
   */
    segurancaRegExp,
    
  /**
   * Endereço da página de login do sistema.
   * (Atenção: não incluir o Contexto do sistema nesse valor)
   */
    loginPath,
  
  /**
   * Endereço da página inicial do sistema, após o login ser efetuado.
   * (Atenção: não incluir o Contexto do sistema nesse valor)
   */
    indexPath,
  
  /**
   * Nome do script de criptografia que será usado para fazer a 
   * criptografia das senhas dos usuários no serviço {@link UsuarioServico}.
   * <br><br>
   * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html">
   *  Está página
   * </a> contém a lista dos scripts padrão da JVM (de implementação obrigatória para as JVMs). 
   * 
   */
    criptografia,
  
  /**
   * SALT para ser usado junto com a senha do usuário no momento que for criada a
   * criptografia da senha.
   */
    criptografiaSalt,
  
  /**
   * Quantidade de iterações da chave quando uma chave de "SecretKey" for usavada.
   * <br><br>
   * Ex.: "PBKDF2WithHmacSHA1"
   */
    criptografiaIteration,
  
  /**
   * Tamanho da chave quando uma chave de "SecretKey" for usavada.
   * <br><br>
   * Ex.: "PBKDF2WithHmacSHA1"
   */
    criptografiaKeyLength,
  
  /**
   * Informa se é necessário fazer a criptografia das senhas dos usuários.
   * <br><br>
   * <b>(Atenção: apenas configure esta opção durante testes!)</b>
   */
    criptografiaAtivo,
  
  /**
   * Informa qual o limite de entidades que podem ser retornadas nas requisições
   * de {@link EntidadesService}.
   * <br><br>
   * Esse limite é usado por proteção, para não buscar informações demais do 
   * banco em uma única requisição.
   */
    limiteEntidadesSize,
    
  /**
   * Informa qual é o tamanho padrão de uma busca (cláusula LIMIT do banco de dados).
   */
    sizeEntidadeDefault,
    
  /**
   * Informa qual a página inicial padrão de uma busca (para encontrar a 
   * cláusula OFFSET do banco de dados).
   */
    pageEntidadeDefault,
  
  /**
   * Informa se o cache de classes, carregado usando o {@link Metamodel} do
   * {@link EntityManager}, deve ser preenchido logo que o sistema iniciar.
   * <br><br>
   * <b>Atenção: a implementação ainda está pendente e as classes serão sempre
   * carregadas apenas quando for necessário (no primeiro uso)!</b>
   */
    preCarregarCache,
  
  /**
   * Informa o nome da unidade de persistência que deve ser usada para criar
   * os gerenciadores de entidades.
   * <br><br>
   * Caso essa configuração seja <code>null</code> ou uma {@link String} vazia,
   * uma unidade aleatória será escolhida como unidade de persistência.
   * <br><br>
   * Caso haja apenas 1 unidade disponível, a seleção aleatória irá selecionar 
   * essa unidade.
   * 
   * <br><br>
   * <b>Atenção: um novo modelo de conexão está sendo criado (usando apenas o
   * hibernate). Essa configuração irá mudar no futuro!</b>
   */
    persistenceUnit,
  
  /**
   * Informa se o carregador do banco de dados deve buscar os arquivos de configurações
   * para inserir informações no banco.
   * <br><br>
   * Normalmente será usado durante o desenvolvimento para inserir informações de teste 
   * no banco.
   * <br><br>
   * Pode ser usado também para inserir as informações iniciais do sistema, quando 
   * usuários padrão devem exeistir, categorias em tabelas devem ser constantes, e outros casos.
   * <br><br>
   * Os nomes infrmados aqui serão os nomes das chaves do arquivo de configuração (um JSON)
   * que serão usados para carregador o banco de dados.
   * 
   * <br><br>
   * <b>Atenção: a implementação ainda está pendente!</b>
   */
    carregarDB,
  
  /**
   * Configurações que serão usadas pelo hibernate quando uma nova Sessão for criada.
   * <br><br>
   * Essas confogurações serão substituidas por novas configurações quando
   * a primeira execução da aplicação acontecer. Essas novas configurações serão
   * guardadas em um arquivo fora do arquivo ".war" ou ".jar", pois serão 
   * informadas só após a 1ª execução (pelo usuário final).
   * <br><br>
   * As configurações de banco de dados serão descartadas temporariamente
   * caso uma tentativa de conexão (com as configurações informadas) tenha
   * falhado. <br>
   * Nesse caso, a aplicação deve iniciar para que seja possível corrigir (ou 
   * informar) as configurações em uma GUI.
   * 
   */
    hibernate,
    
  /**
   * Informa se é necessário carregar as classes de persistência automaticamente.
   * <br><br>
   * Por padrão é <code>true</code>.
   */
    hibernateAutoLoad,
    
  /**
   * Nome das classes que deverão ser carregadas par a unidade de persistência.
   * <br>
   * Essas serão as entidades do sistema, que o hibernate irá administratar.
   */
    entidadesClasses,
    
  /**
   * Diretório externo a aplicação, onde será guardo os arquivos de configuração
   * com informações fornecidades pelo usuário final.
   * <br><br>
   * O padrão é <code>System.getProperty("user.home")</code>.
   */
    configDir,
    
  }
  
  //==========================================================================
  //==========================================================================
  //==========================================================================
  
  
  // constantes do sistema
  //public static final String JSON_PERSISTENCIA = "application/json";
  
  
  //==========================================================================
  //==========================================================================
  //==========================================================================
  
  public static void load(Map<String,Object> props){
    Object obj;
    int mods;
    int mask = Modifier.PUBLIC | Modifier.STATIC;
    
    for (Field field : Configuracoes.class.getFields()){
      mods = field.getModifiers();
      if ((mods ^ mask) != 0) continue;
      obj = props.get(field.getName());
      if( obj == null ) continue;
      try {

        Class<?> t = field.getType();
        if( obj instanceof String ){
          String str = (String)obj;
          if (field.getType().isPrimitive() ) {
            if (t.equals(boolean.class)) {
              field.setBoolean(Configuracoes.class, Boolean.parseBoolean(str));
            } else if (t.equals(byte.class)) {
              field.setByte(Configuracoes.class, Byte.parseByte(str));
            } else if (t.equals(char.class)) {
              field.setChar(Configuracoes.class, str.charAt(0));
            } else if (t.equals(short.class)) {
              field.setShort(Configuracoes.class, Short.parseShort(str));
            } else if (t.equals(int.class)) {
              field.setInt(Configuracoes.class, Integer.parseInt(str));
            } else if (t.equals(long.class)) {
              field.setLong(Configuracoes.class, Long.parseLong(str));
            } else if (t.equals(float.class)) {
              field.setFloat(Configuracoes.class, Float.parseFloat(str));
            } else if (t.equals(double.class)) {
              field.setDouble(Configuracoes.class, Double.parseDouble(str));
            }
          } else if(t.isArray()){
            field.set(Configuracoes.class, str.split("[,;\\s]+") );
          } else if(t.isAssignableFrom(str.getClass())){
            field.set(Configuracoes.class, str);
          }
        }else{
          if(t.isArray()){
            
          }else if(t.isAssignableFrom(Collection.class)){
            Collection para = (Collection)field.get(Configuracoes.class);
            Collection from = (Collection)props.get(field.getName());
            para.addAll( from );
          }else if(t.isAssignableFrom(Map.class)){
            Map mapa = (Map)field.get(Configuracoes.class);
            //mapa.put(obj, obj);
          }else{
            field.set(Configuracoes.class, obj);
          }
        }
        

      } catch (IllegalAccessException ex) {
        System.err.println(String
                .format("--->>  Não é possível fazer reflexão nos atributos da classe 'Configuracoes': %s\n",
                        ex.getMessage()));
      }
    }
  }
  
  
  
  
  public Object get(Key key){
    return get(key.name());
  }
  public Object get(String key){
    return configs.get(key);
  }
  
  
  
  public String JSON_PERSISTENCIA(){
    return (String) configs.get(Key.JSON_PERSISTENCIA.name());
  }
  public String loginCookieName(){
    return (String) configs.get(Key.loginCookieName.name());
  }
  public String rewriteRegExp(){
    return (String) configs.get(Key.rewriteRegExp.name());
  }
  public String segurancaRegExp(){
    return (String) configs.get(Key.segurancaRegExp.name());
  }
  public String loginPath(){
    return (String) configs.get(Key.loginPath.name());
  }
  public String indexPath(){
    return (String) configs.get(Key.indexPath.name());
  }
  
  
  public String criptografia(){
    return (String) configs.get(Key.criptografia.name());
  }
  public String criptografiaSalt(){
    return (String) configs.get(Key.criptografiaSalt.name());
  }
  public Integer criptografiaIteration(){
    return (Integer) configs.get(Key.criptografiaIteration.name());
  }
  public Integer criptografiaKeyLength(){
    return (Integer) configs.get(Key.criptografiaKeyLength.name());
  }
  public Boolean criptografiaAtivo(){
    return (Boolean) configs.get(Key.criptografiaAtivo.name());
  }
  
  
  public Integer limiteEntidadesSize(){
    return (Integer) configs.get(Key.limiteEntidadesSize.name());
  }
  public Integer sizeEntidadeDefault(){
    return (Integer) configs.get(Key.sizeEntidadeDefault.name());
  }
  public Integer pageEntidadeDefault(){
    return (Integer) configs.get(Key.pageEntidadeDefault.name());
  }
  public Boolean preCarregarCache(){
    return (Boolean) configs.get(Key.preCarregarCache.name());
  }
  public String persistenceUnit(){
    return (String) configs.get(Key.persistenceUnit.name());
  }
  public Collection<String> carregarDB(){
    return (Collection<String>) configs.get(Key.carregarDB.name());
  }
  public Map<String,Object> hibernate(){
    return (Map<String,Object>) configs.get(Key.hibernate.name());
  }
  
  public Boolean hibernateAutoLoad(){
    return (Boolean) configs.get(Key.hibernateAutoLoad.name());
  }
  
  public Collection<String> entidadesClasses(){
    return (Collection<String>) configs.get(Key.entidadesClasses.name());
  }
  
  public String configDir(){
    return (String) configs.get(Key.configDir.name());
  }
  
}
