package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.Service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private BcryptService bcryptService;
    private UserService userService;
    private String symbol = "---------------";

    DatabaseSeeder(UserService userService, BcryptService bcryptService){
        this.userService = userService;
        this.bcryptService = bcryptService;
    }

    @Override
    public void run(String... args) throws Exception {
        createUser();
    }

    private void createAdmin(){
    }

    private void createUser() throws Exception {
        printer("SEEDING USER");

        Boolean isUserExists = userService.isUserExists(null, Constants.DEFAULT_USER_EMAIL, Constants.DEFAULT_USER_PHONENUMBER);
        if(isUserExists){
            printer("DEFAULT USER ALREADY EXISTS");
        }else{
            String hashPassword = bcryptService.generateEncodedPassword(Constants.DEFAULT_USER_PASSWORD);
            userService.createUser(Constants.DEFAULT_USER_EMAIL, Constants.DEFAULT_USER_NAME,
                    Constants.DEFAULT_USER_PASSWORD, hashPassword, "");
            printer("DEFAULT USER CREATED");
        }
    }

    private void printer(String content){
        System.out.println(symbol +" " + content + " " + symbol);
    }
}
