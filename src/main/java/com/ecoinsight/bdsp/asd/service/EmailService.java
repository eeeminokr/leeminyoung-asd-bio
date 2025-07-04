package com.ecoinsight.bdsp.asd.service;


import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.ecoinsight.bdsp.asd.entity.Mail;
import com.ecoinsight.bdsp.asd.model.SentEmailEvent;

@Service

public class EmailService  {
    

private static JavaMailSender javaMailSender;

@Autowired
private  SpringTemplateEngine templateEngine;

@Autowired
private SmtMailSenderService mailSenderService;

@Value("${spring.mail.username}") private String _mailSenderAddress;

@Value("${spring.mail.password}") private String _mailSenderAddressPassword;

// @ConstructorProperties({"JavaMailSender","SpringTemplateEngine"})
// public EmailService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine){
//                 this.javaMailSender = javaMailSender;
//                 this.templateEngine = templateEngine;
                
    
// }

private final Logger LOGGER = LoggerFactory.getLogger(getClass());

@Async("asyncExecutor")
public void emailSend(SentEmailEvent event) throws MailException, MessagingException{
 
    // JavaMailSender mailSender = mailSenderService.getJavaMailSender("sangwong618@gmail.com","oxsrvkdkhiiqwnxo"); // 다른 email 계정으로 사용시에, 동적으로 메일 기입하면 됨.
    JavaMailSender mailSender = mailSenderService.getJavaMailSender(_mailSenderAddress,_mailSenderAddressPassword);
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        // helper.setFrom("dkdl6522@gmail.com");
        helper.setSubject(event.getTitle());
        // helper.setFrom(mailSender.getClass().ge);
      
        Context context = new Context();

        event.getContent().forEach((key, value) ->{

            context.setVariable(key, value);

        });


        String html = templateEngine.process("mail", context);
        


        
         helper.setText(html,true);
         
         List<String> emailArr = new ArrayList<>();
        for(int i=0; i< event.getToEmailArray().size(); i++){

      
             emailArr.add(event.getToEmailArray().get(i));
      
          

               LOGGER.debug("splitEmail={}", event.getToEmailArray().get(i));
        }
        
    
         helper.setTo(event.getToEmailArray().toArray(new String[event.getToEmailArray().size()])); // 해당 여러 이메일이 담긴 리스트를 배열로 담기 위한 로직
        
         mailSender.send(message);


  
  
        // 리스트를 배열로 만들어준다.

        //  매개변수로 T[] a 얘를 쓴다. 

        // 즉, message.setTo(emailList.toArray(new String[emailList.size()]));

        // 리스트를 배열로 만들건데 문자열 배열로 만들어주세요. 리스트 사이즈 만큼 변환할 것이다. 라는 뜻이다.


  
        // values.forEach((key, value)->{
        //     context.setVariable(key, value);
        // });

 
        // String html = templateEngine.process(templateName, context);
        // helper.setText(html, true);

        // //메일 보내기
        // javaMailSender.send(message);


}







}







