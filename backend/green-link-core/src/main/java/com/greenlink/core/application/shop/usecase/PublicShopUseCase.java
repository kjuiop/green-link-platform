package com.greenlink.core.application.shop.usecase;

import com.greenlink.core.application.shop.dto.PlaceShopOrderCommand;
import com.greenlink.core.application.shop.dto.PlacedShopOrderResult;
import com.greenlink.core.application.shop.dto.PublicOrderStatusView;
import com.greenlink.core.application.shop.dto.PublicProductView;
import com.greenlink.core.application.shop.dto.PublicShopView;

public interface PublicShopUseCase {

    PublicShopView getShop(String slug);

    PublicProductView getProduct(String slug, Long productId);

    PlacedShopOrderResult placeOrder(String slug, PlaceShopOrderCommand command, String clientIp);

    PublicOrderStatusView lookupOrder(String slug, String orderNo, String ordererPhone, String clientIp);
}
