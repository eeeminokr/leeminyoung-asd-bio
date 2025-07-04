package com.ecoinsight.bdsp.asd.service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class SmtMailSenderService implements IMailSenderService{
    

  @Value("smtp.gmail.com")
   private String host;
   @Value("587")
   private int port;
   @Value("true")
   private String auth;
   @Value("true")
   private String debug;
   @Value("true")
   private String enable;
   @Value("UTF-8")
   private String charset;
   @Value("smtp")
   private String protocol;


   @Override
   public JavaMailSender getJavaMailSender(final String email,final String password) {

          JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            
            mailSender.setHost(host);
            mailSender.setUsername(email);
            mailSender.setPassword(password);
            mailSender.setPort(port);
         Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth",auth);
            props.put("mail.debug",debug);
            props.put("mail.smtp.starttls.enable",enable);
            props.put("mail.mime.charset",charset);
            props.put("mail.transport.protocol",protocol);         
          
            return mailSender;
   }


}
