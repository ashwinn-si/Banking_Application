package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.UserDTO;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Repository.UserRepository;
import com.ashwinsi.bankingApplication.Utils.BcryptService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isUserExists(UUID userId, String email, String phoneNumber) throws Exception{
        Optional<User> user;
        if(userId != null) {
            user = userRepository.findById(userId);
        }else if(email != null){
            user = userRepository.findByEmail(email);
        }else{
            user = userRepository.findByPhoneNumber(phoneNumber);
        }
       return user.isPresent();
    }

    protected User findUser(UUID userId, String email, String phoneNumber) throws  Exception{
        User user;
        if(userId != null) {
            user = userRepository.findById(userId).orElseThrow(() -> new CustomError("User Not Found", HttpStatus.NOT_FOUND));
        }else if(email != null){
            user = userRepository.findByEmail(email).orElseThrow(() -> new CustomError("User Not Found", HttpStatus.NOT_FOUND));
        }else{
            user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new CustomError("User Not Found", HttpStatus.NOT_FOUND));
        }
        return user;
    }

    @Transactional
    public UserDTO createUser(String email, String name, String phoneNumber, String password, String address){
        String hashPassword = passwordEncoder.encode(password);

        User user = new User(name, email, hashPassword, phoneNumber, address);

        User savedUser = userRepository.save(user);

        return new UserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getPhoneNumber(), savedUser.getPassword(), savedUser.isActivated());
    }
}
