package com.greenlink.core.application.shop.service;

import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopSlug;
import com.greenlink.core.domain.shop.port.ShopRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.stereotype.Component;

/**
 * 공개 요청에서 쇼핑몰(회사)을 결정하는 유일한 지점이다.
 * 서브도메인·고객 도메인을 도입할 때 호스트명 기반 조회를 여기에 추가한다.
 */
@Component
public class ShopLookup {

    private final ShopRepository shopRepository;

    public ShopLookup(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop findPublished(String slug) {
        String normalized = ShopSlug.normalizeOrNull(slug);
        if (normalized == null) {
            throw new BusinessException(ShopErrorCode.SHOP_NOT_FOUND);
        }
        return shopRepository.findBySlug(normalized)
                .filter(Shop::isPublished)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.SHOP_NOT_FOUND));
    }
}
