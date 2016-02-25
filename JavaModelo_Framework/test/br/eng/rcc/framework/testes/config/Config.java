
package br.eng.rcc.framework.testes.config;

import org.jboss.weld.environment.se.StartMain;

public class Config {
  
  private static boolean jpaIniciado;
  private static boolean bancoIniciado;
  private static boolean cdiIniciado;
  private static boolean jaxrsIniciado; // não é usado por enquanto
  
  
  public static void initConfig(){
    System.out.printf("Iniciando configurações do sistema: \n");
    if( !cdiIniciado ){
      //System.out.printf("- Iniciando CDI com Weld \n");
      //System.setProperty("org.jboss.weld.development", "true");
      //System.setProperty("org.jboss.logging.provider", "jdk");
      //StartMain.main(new String[0]);
      cdiIniciado = true;
    }
    if( !jpaIniciado ){
      System.out.printf("- Iniciando JPA com Hibernate \n");
      jpaIniciado = true;
    }
    if( !bancoIniciado ){
      System.out.printf("- Iniciando Banco com H2 \n");
      bancoIniciado = true;
    }
  }
  
}
