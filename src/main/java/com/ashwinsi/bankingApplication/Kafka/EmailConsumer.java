package com.ashwinsi.bankingApplication.Kafka;

import com.ashwinsi.bankingApplication.Config.EnvConfig;
import com.ashwinsi.bankingApplication.DTO.EmailContentDTO;
import com.ashwinsi.bankingApplication.DTO.EmailEventDTO;
import com.ashwinsi.bankingApplication.DTO.Enum.EmailType;
import com.ashwinsi.bankingApplication.Utils.Constants;
import com.ashwinsi.bankingApplication.Utils.EmailContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {
    private  final JavaMailSender javaMailSender;
    private final EnvConfig envConfig;
    private final EmailContextService emailContextService;


    @KafkaListener(
            topics = Constants.KAFKA_EMAIL_TOPIC,
            groupId = "banking-email-group",
            concurrency = "3"
    )
    public void handleEmailEvent(
            EmailEventDTO event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, // which Kafka partition the message came from.
            @Header(KafkaHeaders.OFFSET) long offset // Kafka track which messages are consumed
    ){
        log.info("Received OTP event from partition={} offset={} for {}",
                partition, offset, event.getToEmail());
        try {
            handleEmailEvent(event);
            log.info("OTP email sent successfully to {}", event.getToEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", event.getToEmail(), e.getMessage());
            throw e;
        }

    }

    public void handleEmailEvent(EmailEventDTO event){
        EmailType type = event.getEmailType();
        String toEmail = event.getToEmail();
        EmailContentDTO emailContentDTO = null;
        String action = event.getAction();

        if(type == EmailType.OTP_EMAIL){
            Integer otp = event.getOtp();
            emailContentDTO = emailContextService.getEmailContent(action, otp);
        }else if(type == EmailType.INFORMATION_EMAIL){
            emailContentDTO = emailContextService.getEmailContent(action);
        }else{
            log.error("ERROR WHILE SENDING EMAIL, EMAIL TYPE %s FOR ACTION %s \n", type,action);
            throw new Error("Email Type not Defined");
        }

        sendEmail(toEmail, emailContentDTO.getSubject(),
                emailContentDTO.getBody(), action);
    }

    public void sendEmail(String receiverEmail, String subject, String body, String action){
        try{
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

            simpleMailMessage.setFrom(envConfig.getMailerSenderEmail());
            simpleMailMessage.setTo(receiverEmail);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);

            javaMailSender.send(simpleMailMessage);
        }catch(Exception e){
            log.error("ERROR WHILE SENDING EMAIL TO %s FOR ACTION %s \n", receiverEmail, action);
            throw e;
        }
    }
}
