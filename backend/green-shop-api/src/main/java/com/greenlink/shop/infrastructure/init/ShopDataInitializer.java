package com.greenlink.shop.infrastructure.init;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;
import com.greenlink.shopcore.domain.menu.port.ShopMenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ShopDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ShopDataInitializer.class);

    private final ShopMenuRepository menuRepository;

    public ShopDataInitializer(ShopMenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (menuRepository.count() > 0) {
            return;
        }
        Set<ShopRole> all = EnumSet.allOf(ShopRole.class);
        menuRepository.save(ShopMenu.create("대시보드", "/dashboard", "dashboard", 1, all));
        menuRepository.save(ShopMenu.create("주문 관리", "/orders", "orders", 2, all));
        menuRepository.save(ShopMenu.create("상품 관리", "/products", "products", 3, all));
        menuRepository.save(ShopMenu.create("쇼핑몰 설정", "/shop-settings", "shop", 4,
            EnumSet.of(ShopRole.SUPER_ADMIN, ShopRole.PARTNER_ADMIN)));
        Set<ShopRole> managers = EnumSet.of(ShopRole.SUPER_ADMIN, ShopRole.PARTNER_ADMIN);
        menuRepository.save(ShopMenu.create("브랜드 관리", "/brands", "brands", 5, managers));
        menuRepository.save(ShopMenu.create("관리자 관리", "/admins", "admins", 6, managers));
        menuRepository.save(ShopMenu.create("온우림 이용 신청", "/onurim", "onurim", 7, EnumSet.of(ShopRole.PARTNER_ADMIN)));
        log.info("shop 기본 메뉴를 생성했습니다.");
    }
}
