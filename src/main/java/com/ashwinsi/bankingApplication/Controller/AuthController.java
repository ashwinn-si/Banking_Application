package com.ashwinsi.bankingApplication.Controller;

import com.ashwinsi.bankingApplication.Service.AuthService;
import com.ashwinsi.bankingApplication.Utils.ResponseHandler;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginDTO{
    @Email
    private String email;
    @Length(min = 1, max = 8, message = "Password Length Must be between 1 and 8")
    private String password;
    @Nullable
    @Length(max = 10, min = 10, message = "Enter valid phone Number")
    private String phoneNumber;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class SignUpDTO{
    private String email;
    private String password;
    private String phoneNumber;
    private String name;
    private String address;
}

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthController {
    AuthService authService;

    AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginController(@RequestBody @Valid LoginDTO loginDTO) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, authService.login(loginDTO.getEmail(), loginDTO.getPhoneNumber(), loginDTO.getPassword()),
                "Login successful", true);
    }

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdminController(@RequestBody @Valid LoginDTO loginDTO) throws Exception {
        return ResponseHandler.handleResponse(HttpStatus.OK, authService.loginAdmin(loginDTO.getEmail(), loginDTO.getPassword()),
                "Login successful", true);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUpController(@RequestBody @Valid SignUpDTO signUpDTO) throws Exception {
        authService.signup(signUpDTO.getEmail(), signUpDTO.getPhoneNumber(), signUpDTO.getPassword(), signUpDTO.getName(), signUpDTO.getAddress());

        return ResponseHandler.handleResponse(HttpStatus.OK, null, "Account Created successfully", true);
    }
}
