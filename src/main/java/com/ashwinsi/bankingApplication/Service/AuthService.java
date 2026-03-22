package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Utils.BcryptService;
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
    private UserService userService;
    private BcryptService bcryptService;
    private JwtService jwtService;

    AuthService(UserService userService, BcryptService bcryptService, JwtService jwtService){
        this.userService = userService;
        this.bcryptService = bcryptService;
        this.jwtService = jwtService;
    }

    public LoginDTO login(String email, String phoneNumber, String password) throws Exception {
        User user = userService.findUser(null, email, phoneNumber);

        Boolean isValidPassword = bcryptService.checkEncodedPassword(user.getPassword(), password);
        if(!isValidPassword){
            throw new CustomError("Incorrect Password", HttpStatus.CONFLICT);
        }

        String jwtToken = jwtService.generateJWTToken(user.getId(), "USER");

        return new LoginDTO(jwtToken);
    }

    public void signup(String email, String phoneNumber, String password, String name, String address) throws Exception{
        //TODO use thread in the future for the db calls
        //TODO use one query to fetch no need separate functions
        if(userService.isUserExists(null, email, null) || userService.isUserExists(null, null, phoneNumber)) {
            throw new CustomError("Email Or PhoneNumber is linked with another user", HttpStatus.CONFLICT);
        }

        String hashPassword = bcryptService.generateEncodedPassword(password);

        userService.createUser(email, name, phoneNumber, hashPassword, address);
    }
}
