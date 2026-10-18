package com.greenlink.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.greenlink")
public class GreenLinkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenLinkApiApplication.class, args);
    }
}
