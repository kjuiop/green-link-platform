package com.greenlink.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.greenlink.shop", "com.greenlink.shopcore"})
public class GreenShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenShopApplication.class, args);
    }
}
