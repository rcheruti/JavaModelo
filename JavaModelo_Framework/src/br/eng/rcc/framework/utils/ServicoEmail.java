
package br.eng.rcc.framework.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;

@RequestScoped
public class ServicoEmail {
  
  private final String subjectPrefix = "";
  private Session sessionEmail = null;
  private String emailLogin = "";
  private final String assinaturaImg = "";
  
  @Inject
  private EntityManager em;
  
  public Session getSession(){
    if( sessionEmail == null ){
      
      /*
      List<Smtp> lista = em.createQuery("SELECT x FROM Smtp x").getResultList();
      if( lista == null  || lista.isEmpty() ){
        throw new MsgException("Não encontramos as configurações de SMTP no banco de dados!");
      }
      Smtp smtp = lista.get(0);
      
      emailLogin = smtp.getEmail();
      String smtpPass = smtp.getSenha();
      boolean tls = smtp.isTls();
      boolean ssl = !smtp.isTls();
      int port = smtp.getPort();
      String host = smtp.getHost();
      /* */
      
      emailLogin = "";
      String smtpPass = "";
      boolean tls = false;
      boolean ssl = !false;
      int port = 0;
      String host = "";
      
      Properties props = new Properties();
      if( tls ) props.put("mail.smtp.starttls.enable",      "true");
      props.put("mail.smtp.host",                           host);
      if( ssl ){
        props.put("mail.smtp.socketFactory.port",           port);
        props.put("mail.smtp.socketFactory.class",          "javax.net.ssl.SSLSocketFactory");
      }
      props.put("mail.smtp.auth",                           "true");
      props.put("mail.smtp.port",                           port);


      sessionEmail = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication( emailLogin, smtpPass);
        }
      });
    }
    return sessionEmail;
  }
  
  public String getEmailFrom(){
    return emailLogin;
  }
  
  
  public MimeMessage newMessage(){
    return new MimeMessage(getSession());
  }
  public void build(MimeMessage message, String msg) 
          throws MessagingException, URISyntaxException, IOException{
    message.setFrom(new InternetAddress( this.getEmailFrom() )); //Remetente
    message.setSubject( subjectPrefix+ message.getSubject() );
    
    if( msg == null ) msg = "";
    
    // Para adicionar o rodapé/assinatura:
    msg += String.format( "<br/><br/><br/><img src=\"cid:%s\" alt\"%s\">", 
            assinaturaImg, "" );
    BodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setContent(msg, "text/html; charset=utf-8");
    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);
    
    Path arquivo = Files.createTempFile(assinaturaImg, ".png");
    Files.copy(this.getClass().getResourceAsStream( assinaturaImg ), 
            arquivo , StandardCopyOption.REPLACE_EXISTING);
    
            
    messageBodyPart = new MimeBodyPart();
    DataSource source = new FileDataSource( arquivo.toFile() );
    messageBodyPart.setDataHandler(new DataHandler(source));
    messageBodyPart.setFileName( assinaturaImg );
    multipart.addBodyPart(messageBodyPart);
    
    message.setContent(multipart, "text/html; charset=utf-8");
  }
  
}
