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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/public/shop/{slug}")
public class PublicShopController {

    private final PublicShopUseCase useCase;
    private final byte[] proxySecret;

    public PublicShopController(PublicShopUseCase useCase,
                                @Value("${shop.public.proxy-secret:}") String proxySecret) {
        this.useCase = useCase;
        this.proxySecret = proxySecret.getBytes(StandardCharsets.UTF_8);
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
        String proxied = http.getHeader("X-Shop-Client-Ip");
        String secret = http.getHeader("X-Shop-Proxy-Secret");
        if (proxySecret.length > 0 && proxied != null && !proxied.isBlank() && secret != null
                && MessageDigest.isEqual(proxySecret, secret.getBytes(StandardCharsets.UTF_8))) {
            return proxied.trim();
        }
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] hops = forwarded.split(",");
            return hops[hops.length - 1].trim();
        }
        return http.getRemoteAddr();
    }
}
