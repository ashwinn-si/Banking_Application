package com.ashwinsi.bankingApplication.Service;

import com.ashwinsi.bankingApplication.DTO.CustomError;
import com.ashwinsi.bankingApplication.Domain.Admin;
import com.ashwinsi.bankingApplication.Repository.AdminRepository;
import com.ashwinsi.bankingApplication.Utils.BcryptService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final BcryptService bcryptService;

    AdminService(AdminRepository adminRepository, BcryptService bcryptService){
        this.adminRepository = adminRepository;
        this.bcryptService = bcryptService;
    }

    @Transactional
    public void createAdmin(String email, String password) throws Exception{
        boolean isAdminExists = isAdminExists(email);
        if(isAdminExists){
            throw new CustomError("Email Already Exists", HttpStatus.CONFLICT);
        }

        String hashPassword = bcryptService.generateEncodedPassword(password);
        Admin admin = new Admin(email, hashPassword);
        adminRepository.save(admin);
    }

    public Admin findAdmin(UUID adminId) throws  Exception{
        Admin admin =  adminRepository.findById(adminId).orElseThrow(() -> new CustomError("Admin Not Found", HttpStatus.NOT_FOUND));
        return admin;
    }

    public Admin findAdmin(String email) throws  Exception{
        Admin admin =  adminRepository.findByEmail(email).orElseThrow(() -> new CustomError("Admin Not Found", HttpStatus.NOT_FOUND));
        return admin;
    }





    public boolean isAdminExists(UUID adminId){
        return adminRepository.findById(adminId).isPresent();
    }

    public boolean isAdminExists(String email){
        return adminRepository.findByEmail(email).isPresent();
    }
}
