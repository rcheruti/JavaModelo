package br.eng.rcc.framework.jaxrs;

import java.io.Serializable;

public class JsonResponse implements Serializable {

  public static final int OK = 200;
  public static final int ERROR_DESLOGADO = 401;
  public static final int ERROR_PERMISSAO = 403;
  public static final int ERROR_DESCONHECIDO = 500;

  public boolean status;
  public Object data;
  public String msg;
  public int codigo;
  public final int page;
  public final int size;

  public JsonResponse() {
    this(true, null);
  }

  public JsonResponse(boolean status, String msg) {
    this(status, null, msg);
  }

  public JsonResponse(boolean status, Object data, String msg) {
    this(status, data, msg, -1, -1);
  }

  public JsonResponse(boolean status, int codigo, Object data, String msg) {
    this(status, codigo, data, msg, -1, -1);
  }

  public JsonResponse(boolean status, Object data, String msg, int page, int size) {
    this(status, OK, data, msg, page, size);
  }

  public JsonResponse(boolean status, int codigo, Object data, String msg, int page, int size) {
    this.status = status;
    this.codigo = codigo;
    this.data = data;
    this.msg = msg;
    this.page = page;
    this.size = size;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getName());
    sb.append("( status=");
    sb.append(this.status);
    sb.append(", data=");
    sb.append(this.data);
    sb.append(", codigo=");
    sb.append(this.codigo);
    sb.append(", msg=");
    sb.append(this.msg);
    sb.append(", page=");
    sb.append(this.page);
    sb.append(", size=");
    sb.append(this.size);
    sb.append(")");
    return sb.toString();
  }
  
}
