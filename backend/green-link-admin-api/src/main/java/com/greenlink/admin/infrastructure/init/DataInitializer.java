package com.greenlink.admin.infrastructure.init;

import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.menu.model.Menu;
import com.greenlink.core.domain.menu.port.MenuRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.init.login-id:admin}")
    private String initLoginId;

    @Value("${admin.init.password}")
    private String initPassword;

    public DataInitializer(AdminRepository adminRepository,
                           MenuRepository menuRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initAdmin();
        initMenus();
    }

    private void initAdmin() {
        if (!adminRepository.existsByLoginId(initLoginId)) {
            adminRepository.save(Admin.create(initLoginId, passwordEncoder.encode(initPassword)));
        }
    }

    private void initMenus() {
        if (!menuRepository.findAll().isEmpty()) {
            return;
        }

        List<AdminRole> all = List.of(AdminRole.SUPER_ADMIN, AdminRole.PARTNER_ADMIN);
        List<AdminRole> superOnly = List.of(AdminRole.SUPER_ADMIN);
        List<CompanyType> anyType = List.of();
        List<CompanyType> ordering = List.of(CompanyType.ORDERING);
        List<CompanyType> vendor = List.of(CompanyType.VENDOR);

        menuRepository.save(Menu.create("대시보드", "/dashboard", 1, null, all, anyType));
        menuRepository.save(Menu.create("신규 발주", "/orders/new", 2, null, all, ordering));
        menuRepository.save(Menu.create("발주 목록", "/orders", 3, null, all, ordering));
        menuRepository.save(Menu.create("수주 목록", "/orders/received", 4, null, all, vendor));

        Menu settings = menuRepository.save(Menu.create("설정", "/settings", 6, null, all, ordering));
        menuRepository.save(Menu.create("문자 발송내역", "/settings/message-log", 1, settings.getId(), all, ordering));
        menuRepository.save(Menu.create("문자 설정", "/settings/messages", 2, settings.getId(), all, ordering));
        menuRepository.save(Menu.create("업체 계정관리", "/settings/vendors", 3, settings.getId(), superOnly, anyType));
        menuRepository.save(Menu.create("메뉴 관리", "/settings/menus", 4, settings.getId(), superOnly, anyType));
    }
}
