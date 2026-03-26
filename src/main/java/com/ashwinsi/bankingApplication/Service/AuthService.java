package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import com.ashwinsi.bankingApplication.Domain.Admin;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Utils.Auditable;
import com.ashwinsi.bankingApplication.Utils.BcryptService;
import com.ashwinsi.bankingApplication.Utils.Constants;
import com.ashwinsi.bankingApplication.Utils.JwtService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginDTO{
    String token;
}

@Service
public class AuthService {
    private final UserService userService;
    private final BcryptService bcryptService;
    private final JwtService jwtService;
    private final AdminService adminService;
    private final AuditLogService auditLogService;

    AuthService(UserService userService, BcryptService bcryptService, JwtService jwtService, AdminService adminService, AuditLogService auditLogService){
        this.userService = userService;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
        this.adminService = adminService;
        this.auditLogService = auditLogService;
    }

    @Auditable(action = Constants.ACTION_LOGIN)
    public LoginDTO login(String email, String phoneNumber, String password) throws Exception {
        User user = userService.findUser(null, email, phoneNumber);

        //checking the user is allowed to login
        boolean isAllowed = auditLogService.isAllowedToPerform(user.getId(), Constants.ACTION_LOGIN);

        if(!isAllowed){
            throw new CustomError("Login attempts has reached a limit kindly try later", HttpStatus.BAD_REQUEST);
        }

        Boolean isValidPassword = bcryptService.checkEncodedPassword(user.getPassword(), password);
        if(!isValidPassword){
            throw new CustomError("Incorrect Password", HttpStatus.CONFLICT);
        }

        String jwtToken = jwtService.generateJWTToken(user.getId(), "USER");

        return new LoginDTO(jwtToken);
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

    public void signup(String email, String phoneNumber, String password, String name, String address) throws Exception{
        if(userService.isUserExists(null, email, null) || userService.isUserExists(null, null, phoneNumber)) {
            throw new CustomError("Email Or PhoneNumber is linked with another user", HttpStatus.CONFLICT);
        }
        userService.createUser(email, name, phoneNumber, password, address);
    }
}
