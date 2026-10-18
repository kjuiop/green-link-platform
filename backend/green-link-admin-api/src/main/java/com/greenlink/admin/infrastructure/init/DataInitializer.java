package com.greenlink.admin.infrastructure.init;

import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.init.login-id:admin}")
    private String initLoginId;

    @Value("${admin.init.password}")
    private String initPassword;

    public DataInitializer(AdminRepository adminRepository,
                            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!adminRepository.existsByLoginId(initLoginId)) {
            adminRepository.save(Admin.create(initLoginId, passwordEncoder.encode(initPassword)));
        }
    }
}
