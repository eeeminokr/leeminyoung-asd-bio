package com.ecoinsight.bdsp.asd.web;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ecoinsight.bdsp.asd.model.SentEmailEvent;
import com.ecoinsight.bdsp.asd.service.EmailService;

@Component
public class MobileBoardEventHandler {

  @Autowired
  private EmailService emailService;

  private final Logger LOGGER = LoggerFactory.getLogger(getClass());

  @Async("asyncExecutor")
  @EventListener(classes = SentEmailEvent.class)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void SendEmail(SentEmailEvent event) throws Exception{

    LOGGER.info("TransactionPhase.AFTER_COMMIT ---> {}", event);

    try {
      emailService.emailSend(event);
    } catch (MailException | MessagingException e) {

      LOGGER.error("Error sent Email to somthing problems", e);
    }

    

  }

}
