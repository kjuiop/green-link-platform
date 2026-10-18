package com.greenlink.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.greenlink")
public class GreenLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenLinkAdminApplication.class, args);
    }
}
