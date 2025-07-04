package com.ecoinsight.bdsp.asd.model;

import java.util.HashMap;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.ecoinsight.bdsp.asd.entity.Mail;


/*
 *  불특정 다수자 게시판 등록 commit transaction after commit  success 후, email evnet data 정보를 전달 할 class 생성.
 */

public class SentEmailEvent extends ApplicationEvent{


    
    private final String title;
    // private String content;
    private final HashMap<String, Object> content;
    private final String templateName;
    private final List<String> toEmailArray;
      private final List<String> RoleNameArray;



    public SentEmailEvent(Object source, Mail mail) {
        super(source);
        this.title = mail.getTitle();
        this.content = mail.getContent();
        this.templateName = mail.getTemplateName();
        this.toEmailArray = mail.getToEmailArray();
        this.RoleNameArray =mail.getRoleNameArray();
    }



    public String getTitle() {
        return title;
    }



    public HashMap<String, Object> getContent() {
        return content;
    }



    public String getTemplateName() {
        return templateName;
    }



    public List<String> getToEmailArray() {
        return toEmailArray;
    }



    public List<String> getRoleNameArray() {
        return RoleNameArray;
    }

    // public SentEmailEvent(Mail mail) {
   
    //     this.mail = mail;
    // }





    // private final String title;
    // // private String content;
    // private final HashMap<String, Object> content;
    // private final String templateName;
    // private final List<String> toArray;  // 수신자 여러명일 경우가 있기때문에 배열로 정의함.
    
    // public SentEmailEvent(Object source, Mail mail) {
    //     super(source);
    //     this.title = mail.getTitle();
    //     this.content = mail.getContent();
    //     this.templateName = mail.getTemplateName();
    //     this.toArray = mail.getToArray();
    // }


    




}

