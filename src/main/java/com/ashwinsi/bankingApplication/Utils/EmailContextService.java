package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.DTO.EmailContentDTO;
import org.springframework.stereotype.Service;

import static com.ashwinsi.bankingApplication.Utils.Constants.*;

@Service
public class EmailContextService {

    public EmailContentDTO getEmailContent(String action, int otp) {
        String subject;
        String body;

        switch (action) {
            case ACTION_USER_SIGNUP:
                subject = "Welcome to Banking Application!";
                body = "Your OTP for signup is: " + otp;
                break;
            case ACTION_LOGIN:
                subject = "Login OTP for Banking Application";
                body = "Your OTP for login is: " + otp;
                break;
            case ACTION_WITHDRAW:
                subject = "Withdrawal OTP for Banking Application";
                body = "Your OTP for withdrawal is: " + otp;
                break;
            case ACTION_TRANSFER:
                subject = "Transfer OTP for Banking Application";
                body = "Your OTP for transfer is: " + otp;
                break;
            default:
                subject = "Banking Application Notification";
                body = "Your OTP is: " + otp;
                break;
        }

        return new EmailContentDTO(subject, body);
    }

    public EmailContentDTO getEmailContent(String action) {
        String subject;
        String body;

        switch (action) {
            case ACTION_INCORRECT_LOGIN:
                subject = "Alert User ! From Bank";
                body = "As someone has attempted to login too much times, you account is turned off from logining";
                break;
            default:
                subject = "Banking Application Notification";
                body = "PODA PUNDA";
                break;
        }

        return new EmailContentDTO(subject, body);
    }
}
