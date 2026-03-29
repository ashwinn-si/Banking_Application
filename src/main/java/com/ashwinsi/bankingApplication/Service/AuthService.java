package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.EmailContentDTO;
import com.ashwinsi.bankingApplication.DTO.UserDTO;
import com.ashwinsi.bankingApplication.Domain.Admin;
import com.ashwinsi.bankingApplication.Domain.Otp;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Utils.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginDTO{
    String token;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ActionIdDTO{
    UUID actionId;
}

@Service
public class AuthService {
    private final UserService userService;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final AdminService adminService;
    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private final EmailServiceWrapper emailService;

    AuthService(UserService userService, BcryptService bcryptService,
                JwtService jwtService, AdminService adminService,
                AuditLogService auditLogService, OtpService otpService,
                EmailServiceWrapper emailService){
        this.userService = userService;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.adminService = adminService;
        this.auditLogService = auditLogService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Auditable(action = Constants.ACTION_LOGIN)
    public ActionIdDTO login(String email, String phoneNumber, String password) throws Exception {
        User user = userService.findUser(null, email, phoneNumber);

        //checking the user is allowed to login
        boolean isAllowed = auditLogService.isAllowedToPerform(user.getId(), Constants.ACTION_LOGIN);

        if(!isAllowed){
            throw new CustomError("Login attempts has reached a limit kindly try later", HttpStatus.BAD_REQUEST);
        }

        Boolean isValidPassword = bcryptService.checkEncodedPassword(user.getPassword(), password);
        if(!isValidPassword){
            boolean isIncorrectPasswordAttemptsExceeded = auditLogService.isAllowedToPerform(user.getId(), Constants.ACTION_INCORRECT_LOGIN);
            if(!isIncorrectPasswordAttemptsExceeded){
                //sending email
                emailService.sendEmail(email, user.getId(), Constants.ACTION_INCORRECT_LOGIN);
                userService.updateUserActiveStatus(user.getId(), false);
            }else{
                auditLogService.updateCache(user.getId(), Constants.ACTION_INCORRECT_LOGIN);
            }
            throw new CustomError("Incorrect Password", HttpStatus.CONFLICT);
        }

        //creating A OTP
        UUID actionId = generateActionId();
        Otp otp = otpService.createOtp(actionId, email, user.getId(), Constants.ACTION_LOGIN);

        //sending email
        emailService.sendEmail(email, user.getId(), Constants.ACTION_LOGIN, otp.getOtp());


        return new ActionIdDTO(actionId);
    }

    public LoginDTO loginAdmin(String email, String password) throws Exception{
        Admin admin = adminService.findAdmin(email);

        Boolean isValidPassword = bcryptService.checkEncodedPassword(admin.getPassword(), password);
        if(!isValidPassword){
            throw new CustomError("Incorrect Password", HttpStatus.CONFLICT);
        }
        String jwtToken = jwtService.generateJWTToken(admin.getId(), "ADMIN");

        return new LoginDTO(jwtToken);
    }

    public ActionIdDTO signup(String email, String phoneNumber, String password, String name, String address) throws Exception{
        Optional<User> user = userService.isUserExistsByEmailOrPhone(email, phoneNumber);
        if(user.isPresent() && user.get().isActivated()) {
            throw new CustomError("Email Or PhoneNumber is linked with another user", HttpStatus.CONFLICT);
        }

        User createdUser;
        if(user.isPresent() && !user.get().isActivated()){
            createdUser = user.get();
        } else {
            createdUser = userService.createUser(email, name, phoneNumber, password, address);
        }

        //creating a otp
        UUID actionId = generateActionId();
        Otp otp = otpService.createOtp(actionId, email, createdUser.getId(), Constants.ACTION_USER_SIGNUP);

        //sending email
        emailService.sendEmail(email, createdUser.getId(), Constants.ACTION_USER_SIGNUP, otp.getOtp());

        return new ActionIdDTO(otp.getActionId());
    }

    public void signupOtp(UUID actionId, Integer otpEntered) throws  Exception{
        boolean isCorrect = otpService.checkOtp(actionId,otpEntered);
        Otp otp = otpService.findOtp(actionId);

        if(isCorrect){
            userService.updateUserActiveStatus(otp.getUserId(), true);
            return;
        }
        throw new CustomError("Incorrect Otp", HttpStatus.BAD_REQUEST);
    }

    public LoginDTO loginOtp(UUID actionId, Integer otpEntered) throws Exception{
        boolean isCorrect = otpService.checkOtp(actionId,otpEntered);
        if(!isCorrect){
            throw new CustomError("Incorrect OTP", HttpStatus.BAD_REQUEST);
        }
        Otp otp = otpService.findOtp(actionId);

        User user = userService.findUser(otp.getUserId(), null, null);

        String jwtToken = jwtService.generateJWTToken(user.getId(), "USER");

        return new LoginDTO(jwtToken);
    }
    
    private UUID generateActionId(){
        return UUID.randomUUID();
    }
}
