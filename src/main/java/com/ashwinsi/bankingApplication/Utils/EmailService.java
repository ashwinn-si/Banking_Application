package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.Config.EnvConfig;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private  final JavaMailSender javaMailSender;
    private final EnvConfig envConfig;

    EmailService(JavaMailSender javaMailSender, EnvConfig envConfig){
        this.javaMailSender = javaMailSender;
        this.envConfig = envConfig;
    }

    public boolean sendEmail(String receiverEmail, String subject, String body, String action){
        try{
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

            simpleMailMessage.setFrom(envConfig.getMailerSenderEmail());
            simpleMailMessage.setTo(receiverEmail);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);

            javaMailSender.send(simpleMailMessage);

            return true;
        }catch(Exception e){
            System.out.printf("ERROR WHILE SENDING EMAIL TO %s FOR ACTION %s \n", receiverEmail, action);
            return false;
        }
    }
}
