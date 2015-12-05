
package br.eng.rcc.framework.seguranca.interfaces;

public interface UsuarioInterface {
    
    static final String usuarioKey = "usuario";
    
    boolean hasPermissao(String chave);
    
    boolean hasGrupo(String chave);
    
}
