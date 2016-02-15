
package br.eng.rcc.framework.testes.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class H2Config {
    
    public void init(){
      try{
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        
        
        conn.close();
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
    
    public static void main(String[] args){
      new H2Config().init();
    }
    
}
