
package br.eng.rcc.framework.jaxrs;


public class MsgException extends RuntimeException{
    
    private String mensagem;
    private int codigo;
    private Object data;
    
    public MsgException(){}
    public MsgException(String msg){
        this(null,msg,null);
    }
    public MsgException(String msg, Throwable t){
        this(null, msg, t);
    }
    public MsgException(Object obj, String msg){
        this(obj, msg, null);
    }
    public MsgException(Object obj, String msg, Throwable t){
        this( 0, obj, msg, t);
    }
    public MsgException(int codigo, Object obj, String msg){
        this(codigo, obj, msg, null);
    }
    public MsgException(int codigo, Object obj, String msg, Throwable t){
        super(msg, t);
        this.mensagem = msg;
        this.data = obj;
        this.codigo = codigo;
    }
    
    
    
    
    @Override
    public String getMessage(){ return this.mensagem; }
    
    //================================================================
    
    public int getCodigo(){ return this.codigo; }
    //public void setCodigo(int param){ this.codigo = param; }
    
    public String getMensagem(){ return this.mensagem; }
    //public void setMensagem(String param){ this.mensagem = param; }
    
    public Object getData(){ return this.data; }
    //public void setData(Object param){ this.data = param; }
    
    
}
