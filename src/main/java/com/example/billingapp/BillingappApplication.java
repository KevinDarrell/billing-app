package com.example.billingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.billingapp")
@EnableScheduling
public class BillingappApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingappApplication.class, args);
       
    }

}
