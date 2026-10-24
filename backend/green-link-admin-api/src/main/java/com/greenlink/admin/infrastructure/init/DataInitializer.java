package com.greenlink.admin.infrastructure.init;

import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.admin.model.Role;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
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

    private static final String DOWNFLOWER_COMPANY_NAME = "다운플라워";

    private final AdminRepository adminRepository;
    private final MenuRepository menuRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.init.login-id:admin}")
    private String initLoginId;

    @Value("${admin.init.password}")
    private String initPassword;

    public DataInitializer(AdminRepository adminRepository,
                           MenuRepository menuRepository,
                           CompanyRepository companyRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.menuRepository = menuRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initOrderingCompany();
        initAdmin();
        initMenus();
    }

    private Company initOrderingCompany() {
        return companyRepository.findByCompanyName(DOWNFLOWER_COMPANY_NAME)
                .orElseGet(() -> companyRepository.save(
                        Company.create(null, DOWNFLOWER_COMPANY_NAME, null, null, null, null, CompanyType.ORDERING)
                ));
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

        // 대시보드 (루트 메뉴, 모든 역할)
        menuRepository.save(Menu.create(
            "대시보드",
            "/dashboard",
            1,
            null,
            List.of(Role.SUPER_ADMIN, Role.ORDERING, Role.VENDOR)
        ));

        // 신규 발주 (루트 메뉴, 관리자+발주자)
        menuRepository.save(Menu.create(
            "신규 발주",
            "/orders/new",
            2,
            null,
            List.of(Role.SUPER_ADMIN, Role.ORDERING)
        ));

        // 발주 목록 (루트 메뉴, 관리자+발주자)
        menuRepository.save(Menu.create(
            "발주 목록",
            "/orders",
            3,
            null,
            List.of(Role.SUPER_ADMIN, Role.ORDERING)
        ));

        // 수주 목록 (루트 메뉴, 모든 역할)
        menuRepository.save(Menu.create(
            "수주 목록",
            "/orders/received",
            4,
            null,
            List.of(Role.SUPER_ADMIN, Role.ORDERING, Role.VENDOR)
        ));

        // 설정 (루트 메뉴, 최고관리자 전용)
        Menu settings = menuRepository.save(Menu.create(
            "설정",
            "/settings",
            6,
            null,
            List.of(Role.SUPER_ADMIN)
        ));

        // 설정 하위: 문자 발송내역
        menuRepository.save(Menu.create(
            "문자 발송내역",
            "/messages",
            1,
            settings.getId(),
            List.of(Role.SUPER_ADMIN, Role.ORDERING)
        ));

        // 설정 하위: 문자 설정
        menuRepository.save(Menu.create(
            "문자 설정",
            "/settings/messages",
            2,
            settings.getId(),
            List.of(Role.SUPER_ADMIN, Role.ORDERING)
        ));

        // 설정 하위: 업체 계정관리
        menuRepository.save(Menu.create(
            "업체 계정관리",
            "/vendors",
            3,
            settings.getId(),
            List.of(Role.SUPER_ADMIN)
        ));

        // 설정 하위: 메뉴 관리
        menuRepository.save(Menu.create(
            "메뉴 관리",
            "/settings/menus",
            4,
            settings.getId(),
            List.of(Role.SUPER_ADMIN)
        ));
    }
}
