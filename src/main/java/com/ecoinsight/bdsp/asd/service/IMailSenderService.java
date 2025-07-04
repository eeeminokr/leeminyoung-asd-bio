package com.ecoinsight.bdsp.asd.service;

import org.springframework.mail.javamail.JavaMailSender;

/*  - JavaMailSender에 메일 발신자 계정 동적으로 변경하기 위해 필요 */
public interface IMailSenderService {
    JavaMailSender getJavaMailSender(String email, String password);
}
