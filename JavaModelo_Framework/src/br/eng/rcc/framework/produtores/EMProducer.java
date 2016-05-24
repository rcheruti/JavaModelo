
package br.eng.rcc.framework.produtores;

import br.eng.rcc.framework.config.Configuracoes;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;

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
    System.out.printf("---  Iniciando config de banco \n");
    
    Properties prop = new Properties();
    for(String key : Configuracoes.hibernate.keySet()){
      prop.put(key, Configuracoes.hibernate.get(key));
    }
    
    List<URL> jarUrls = new ArrayList<>();
    jarUrls.add( this.getClass().getClassLoader().getResource("../lib/JavaModelo_Entities.jar") );
    
    PersistenceUnitInfoImpl puInfo = 
            new PersistenceUnitInfoImpl("PersistenciaPU", 
                    Arrays.asList(
                            //"br.eng.rcc.framework.seguranca.entidades.Credencial",
                            //"br.eng.rcc.framework.seguranca.entidades.ChaveAcesso",
                            //"br.eng.rcc.framework.seguranca.entidades.Grupo",
                            //"br.eng.rcc.framework.seguranca.entidades.Permissao",
                            //"br.eng.rcc.framework.seguranca.entidades.SegUsuario",
                            "br.eng.rcc.framework.seguranca.jaxrs.LoginService")
                    , prop, jarUrls);
    
    EntityManagerFactoryBuilder emFB = 
      Bootstrap.getEntityManagerFactoryBuilder(
        new PersistenceUnitInfoDescriptor(puInfo), Collections.emptyMap(), 
              this.getClass().getClassLoader());
    
    URL url = this.getClass().getClassLoader().getResource("../lib/JavaModelo_Entities.jar");
    System.out.printf("---  Res URL: %s \n", url );
    
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
