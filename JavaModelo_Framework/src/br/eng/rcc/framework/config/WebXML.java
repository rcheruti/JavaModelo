package br.eng.rcc.framework.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
    int mods;
    int mask = Modifier.PUBLIC | Modifier.STATIC;

    for (Field field : Configuracoes.class.getFields()) {
      mods = field.getModifiers();
      if ((mods ^ mask) != 0) {
        continue;
      }

      str = ctx.getInitParameter(prefix + field.getName());
      if (str == null) {
        continue;
      }
      str = str.trim();
      try {

        Class<?> t = field.getType();
        if (field.getType().isPrimitive()) {
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
        } else {
          field.set(Configuracoes.class, str);
        }

      } catch (IllegalAccessException ex) {
        System.err.println(String
                .format("--->>  Não é possível fazer reflexão nos atributos da classe 'Configuracoes': %s\n",
                        ex.getMessage()));
      }
    }
    
    System.out.printf("---  Configurações de WebXML finalizado. \n");
    
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }

}
