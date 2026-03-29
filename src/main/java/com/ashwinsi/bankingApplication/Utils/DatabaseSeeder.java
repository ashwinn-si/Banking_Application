package com.ashwinsi.bankingApplication.Utils;

import com.ashwinsi.bankingApplication.Service.AdminService;
import com.ashwinsi.bankingApplication.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {
    private UserService userService;
    private AdminService adminService;
    private String symbol = "---------------";

    DatabaseSeeder(UserService userService, AdminService adminService){
        this.userService = userService;
        this.adminService = adminService;
    }

    @Override
    public void run(String... args) throws Exception {
        createUser();
        createAdmin();
    }

    private void createAdmin() throws Exception {
        printer("SEEDING ADMIN");

        Boolean isAdminExists = adminService.isAdminExists(Constants.DEFAULT_ADMIN_EMAIL);

        if(isAdminExists){
            printer("DEFAULT ADMIN ALREADY EXISTS");
        }else{
            adminService.createAdmin(Constants.DEFAULT_ADMIN_EMAIL, Constants.DEFAULT_ADMIN_PASSWORD);
            printer("DEFAULT ADMIN CREATED");
        }
    }

    private void createUser() throws Exception {
        printer("SEEDING USER");

        boolean isUserEmailExists = userService.isUserExists(null, Constants.DEFAULT_USER_EMAIL, null);
        boolean isUserPhoneExists = userService.isUserExists(null, null, Constants.DEFAULT_USER_PHONENUMBER);

        if(isUserEmailExists || isUserPhoneExists){
            printer("DEFAULT USER ALREADY EXISTS");
        }else{
            userService.createDefaultUser(Constants.DEFAULT_USER_EMAIL, Constants.DEFAULT_USER_NAME,
                    Constants.DEFAULT_USER_PHONENUMBER, Constants.DEFAULT_USER_PASSWORD, "");
            printer("DEFAULT USER CREATED");
        }
    }

    private void printer(String content){
        log.info(symbol +" " + content);
    }
}
