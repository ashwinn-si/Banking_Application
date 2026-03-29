package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.DTO.UserDTO;
import com.ashwinsi.bankingApplication.Domain.User;
import com.ashwinsi.bankingApplication.Repository.UserRepository;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void updateUserActiveStatus(UUID userId, boolean flag) throws  Exception{
        User user = findUser(userId, null, null);
        user.setActivated(flag);
    }

    public boolean isUserExists(UUID userId, String email, String phoneNumber) {
        Optional<User> user;
        if(userId != null) {
            user = userRepository.findById(userId);
        }else if(email != null){
            user = userRepository.findByEmailAndIsActivated(email, true);
        }else{
            user = userRepository.findByPhoneNumberAndIsActivated(phoneNumber, true);
        }
       return user.isPresent();
    }

    public Optional<User> isUserExistsByEmailOrPhone(String email, String phoneNumber){
        return userRepository.findByEmailOrPhoneNumber(email, phoneNumber);
    }


    protected User findUser(UUID userId, String email, String phoneNumber) throws  Exception{
        User user;
        if(userId != null) {
            user = userRepository.findById(userId).orElseThrow(() -> new CustomError("User Not Found", HttpStatus.NOT_FOUND));
        }else if(email != null){
            user = userRepository.findByEmailAndIsActivated(email, true).orElseThrow(() -> new CustomError("User Not Found", HttpStatus.NOT_FOUND));
        }else{
            user = userRepository.findByEmailAndIsActivated(phoneNumber, true).orElseThrow(() -> new CustomError("User Not Found", HttpStatus.NOT_FOUND));
        }
        return user;
    }

    @Transactional
    public User createUser(String email, String name, String phoneNumber, String password, String address){
        String hashPassword = passwordEncoder.encode(password);

        User user = new User(name, email, hashPassword, phoneNumber, address);

        User savedUser = userRepository.save(user);

        return savedUser;
    }

    @Transactional
    public UserDTO createDefaultUser(String email, String name, String phoneNumber, String password, String address){
        String hashPassword = passwordEncoder.encode(password);

        User user = new User(name, email, hashPassword, phoneNumber, address);

        user.setActivated(true);

        User savedUser = userRepository.save(user);

        return new UserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getPhoneNumber(), savedUser.getPassword(), savedUser.isActivated());
    }
}
