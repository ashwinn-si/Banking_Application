package com.ashwinsi.bankingApplication.Controller;

import com.ashwinsi.bankingApplication.Domain.Otp;
import com.ashwinsi.bankingApplication.Service.AuthService;
import com.ashwinsi.bankingApplication.Service.EmailServiceWrapper;
import com.ashwinsi.bankingApplication.Service.OtpService;
import com.ashwinsi.bankingApplication.Utils.ResponseHandler;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class OtpDTO {
    private UUID actionId;
    private Integer otp;
}


@RestController
@RequestMapping("/api/otp")
public class OtpController {
    private AuthService authService;
    private OtpService otpService;
    private EmailServiceWrapper emailServiceWrapper;

    OtpController(AuthService authService, OtpService otpService,
            EmailServiceWrapper emailServiceWrapper) {
        this.authService = authService;
        this.otpService = otpService;
        this.emailServiceWrapper = emailServiceWrapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginController(@RequestBody @Valid OtpDTO otpDTO) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK,
                authService.loginOtp(otpDTO.getActionId(), otpDTO.getOtp()), "Login Successful",
                true);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupController(@RequestBody @Valid OtpDTO otpDTO) throws Exception {
        authService.signupOtp(otpDTO.getActionId(), otpDTO.getOtp());
        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Signup Succesfull", true);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendOtpController(@RequestBody @Valid OtpDTO otpResendDTO)
            throws Exception {
        Otp otp = otpService.findOtp(otpResendDTO.getActionId());

        otpService.resendOtp(otpResendDTO.getActionId());
        emailServiceWrapper.sendEmail(otp.getReceiverEmail(), otp.getUserId(), otp.getAction(),
                otp.getOtp());

        return ResponseHandler.handleResponse(HttpStatus.OK, null, "OTP sent successfully", true);
    }
}
