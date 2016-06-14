package br.eng.rcc.framework.persistencia;

public class MsgException extends RuntimeException {

  private String mensagem;
  private int codigo;
  private Object data;

  public MsgException() {
  }

  public MsgException(String msg) {
    this(null, msg, null);
  }

  public MsgException(String msg, Throwable t) {
    this(null, msg, t);
  }

  public MsgException(Object obj, String msg) {
    this(obj, msg, null);
  }

  public MsgException(Object obj, String msg, Throwable t) {
    this(0, obj, msg, t);
  }

  public MsgException(int codigo, Object obj, String msg) {
    this(codigo, obj, msg, null);
  }

  public MsgException(int codigo, Object obj, String msg, Throwable t) {
    super(msg, t);
    this.mensagem = msg;
    this.data = obj;
    this.codigo = codigo;
  }

  @Override
  public String getMessage() {
    return this.mensagem;
  }

  //================================================================
  /**
   * Retorna o código de resposta interno do sistema. <br>
   * Alguns códigos são padrões de todos os sistemas. <br>
   * <br>
   * Ex.: <br>
   * - 401 informa que o usuário não está logado <br>
   * - 403 informa que o usuário não tem permissão <br>
   * - 500 informa um desconhecido erro que acabou de acontecer no servidor <br>
   * 
   * @return O código de resposta
   */
  public int getCodigo() {
    return this.codigo;
  }
  //public void setCodigo(int param){ this.codigo = param; }
  
  /**
   * Uma mensagem que informa o que é ou qual foi o problema. <br>
   * 
   * @return 
   */
  public String getMensagem() {
    return this.mensagem;
  }
  //public void setMensagem(String param){ this.mensagem = param; }
  
  /**
   * Um objeto que é enviado para o cliente para ajudar a contornar o problema
   * no cliente. <br>
   * 
   * @return 
   */
  public Object getData() {
    return this.data;
  }
  //public void setData(Object param){ this.data = param; }

}
