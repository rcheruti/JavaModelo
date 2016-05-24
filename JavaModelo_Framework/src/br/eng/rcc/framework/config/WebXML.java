package br.eng.rcc.framework.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Essa classe será usada para carregar as configurações que estiverem
 * disponíveis no arquivo "/WEB-INF/web.xml" desse sistema.
 *
 * @author rcheruti
 */
@WebListener
public class WebXML implements ServletContextListener {
  
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.printf("---  Carregando configurações do arquivo WebXML. \n");
    ServletContext ctx = sce.getServletContext();

    String str;
    String prefix = "Configuracoes.";
    Map<String,Object> props = new HashMap<>(12);
    
    for (Field field : Configuracoes.class.getFields()) {
      str = ctx.getInitParameter(prefix + field.getName());
      if (str != null) {
        props.put(field.getName(), str);
      }
    }
    Configuracoes.load(props);
    
    System.out.printf("---  Configurações de WebXML finalizado. \n");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }

}
