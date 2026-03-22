package com.ashwinsi.bankingApplication.Utils.Seeders;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("========USER SEEDER==============");
    }
}
