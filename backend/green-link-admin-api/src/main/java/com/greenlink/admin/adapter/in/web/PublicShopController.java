package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.LookupShopOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.PlaceShopOrderRequest;
import com.greenlink.core.application.shop.dto.PlacedShopOrderResult;
import com.greenlink.core.application.shop.dto.PublicOrderStatusView;
import com.greenlink.core.application.shop.dto.PublicProductView;
import com.greenlink.core.application.shop.dto.PublicShopView;
import com.greenlink.core.application.shop.usecase.PublicShopUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/shop/{slug}")
public class PublicShopController {

    private final PublicShopUseCase useCase;

    public PublicShopController(PublicShopUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<PublicShopView> getShop(@PathVariable String slug) {
        return ResponseEntity.ok(useCase.getShop(slug));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<PublicProductView> getProduct(@PathVariable String slug, @PathVariable Long productId) {
        return ResponseEntity.ok(useCase.getProduct(slug, productId));
    }

    @PostMapping("/orders")
    public ResponseEntity<PlacedShopOrderResult> placeOrder(@PathVariable String slug,
                                                            @RequestBody @Valid PlaceShopOrderRequest req,
                                                            HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.placeOrder(slug, req.toCommand(), clientIp(http)));
    }

    @PostMapping("/orders/lookup")
    public ResponseEntity<PublicOrderStatusView> lookup(@PathVariable String slug,
                                                        @RequestBody @Valid LookupShopOrderRequest req,
                                                        HttpServletRequest http) {
        return ResponseEntity.ok(useCase.lookupOrder(slug, req.orderNo(), req.ordererPhone(), clientIp(http)));
    }

    private String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            return hops[hops.length - 1].trim();
        }
        return http.getRemoteAddr();
    }
}
