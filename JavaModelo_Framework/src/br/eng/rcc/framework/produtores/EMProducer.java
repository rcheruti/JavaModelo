
package br.eng.rcc.framework.produtores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

@ApplicationScoped
public class EMProducer {
  
  //@PersistenceContext
  //@Produces
  private EntityManager em;
  
  private SessionFactory sf;
  
  //@PersistenceUnit
  private EntityManagerFactory emf;
  private boolean myEMF;

  @PostConstruct
  public void postConstruct(){
    
    Properties prop = new Properties();
    prop.put("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
    prop.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
    prop.put("hibernate.connection.url", "jdbc:mysql://127.0.0.1:3306/javamodelo_test");
    prop.put("hibernate.connection.username", "root");
    prop.put("hibernate.connection.password", "root");
    prop.put("hibernate.c3p0.min_size", "5");
    prop.put("hibernate.c3p0.max_size", "20");
    
    PersistenceUnitInfoImpl puInfo = 
            new PersistenceUnitInfoImpl("myPU", new ArrayList<>(), prop);
    
    EntityManagerFactoryBuilderImpl emFB = 
      new EntityManagerFactoryBuilderImpl(
        new PersistenceUnitInfoDescriptor(puInfo), null);
    
    emf = emFB.build();
    
    System.out.printf("---  Config de banco pronta! \n");
    
  }
  
  @PreDestroy
  public void preDestroy(){
    if( myEMF && emf.isOpen() ){
      emf.close();
    }
  }

  @Produces
  public EntityManager produceEM(){
    return emf.createEntityManager();
  }
  
            // @Disposes
  public void disposeEM( EntityManager em){
    if( em.isOpen() ){
      em.flush();
      try{
        if( em.getTransaction().isActive() ){
          em.getTransaction().commit();
        }
      }catch(IllegalStateException ex){
        
      }
      em.close();
    }
  }
    
}
