package com.hyundai.dms;

import com.hyundai.dms.service.DataSeederService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }

/* 
    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initData(DataSeederService dataSeederService) {
        return args -> {
            System.out.println("[INIT] Running data seeder to update records...");
            dataSeederService.seedAll();
        };
    }
    */
}
