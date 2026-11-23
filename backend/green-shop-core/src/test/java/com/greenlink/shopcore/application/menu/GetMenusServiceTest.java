package com.greenlink.shopcore.application.menu;

import com.greenlink.shopcore.application.menu.service.GetMenusService;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;
import com.greenlink.shopcore.domain.menu.port.ShopMenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetMenusServiceTest {

    @Mock ShopMenuRepository menuRepository;

    @Test
    void 역할의_메뉴를_정렬_순서대로_반환한다() {
        ShopMenu second = ShopMenu.of(2L, "주문 관리", "/orders", "orders", 2, Set.of(ShopRole.SHOP_ADMIN));
        ShopMenu first = ShopMenu.of(1L, "대시보드", "/dashboard", "dashboard", 1, Set.of(ShopRole.SHOP_ADMIN));
        given(menuRepository.findByRole(ShopRole.SHOP_ADMIN)).willReturn(List.of(second, first));

        List<ShopMenu> result = new GetMenusService(menuRepository).getMenus(ShopRole.SHOP_ADMIN);

        assertThat(result).extracting(ShopMenu::getName).containsExactly("대시보드", "주문 관리");
    }
}
