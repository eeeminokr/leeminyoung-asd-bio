package com.ecoinsight.bdsp.asd.entity;

import java.util.HashMap;
import java.util.List;

public class Mail {
   
    private String title;
    // private String content;
    private HashMap<String, Object> content;
    private String templateName;
    private List<String> toEmailArray;  // 수신자 여러명일 경우가 있기때문에 배열로 정의함.
    private List<String> RoleNameArray;



    public void setTitle(String title) {
        this.title = title;
    }
        public String getTitle() {
        return title;
    }
    // public String getContent() {
    //     return content;
    // }
    // public void setContent(String content) {
    //     this.content = content;
    // }
    public HashMap<String, Object> getContent() {
        return content;
    }
    public void setContent(HashMap<String, Object> content) {
        this.content = content;
    }
    public String getTemplateName() {
        return templateName;
    }
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }



    public List<String> getToEmailArray() {
        return toEmailArray;
    }
    public void setToEmailArray(List<String> toEmailArray) {
        this.toEmailArray = toEmailArray;
    }
    public List<String> getRoleNameArray() {
        return RoleNameArray;
    }
    public void setRoleNameArray(List<String> roleNameArray) {
        RoleNameArray = roleNameArray;
    }

}
