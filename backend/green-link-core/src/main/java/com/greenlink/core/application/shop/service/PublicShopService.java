package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.shop.dto.PlaceShopOrderCommand;
import com.greenlink.core.application.shop.dto.PlacedShopOrderResult;
import com.greenlink.core.application.shop.dto.PublicOrderStatusView;
import com.greenlink.core.application.shop.dto.PublicProductView;
import com.greenlink.core.application.shop.dto.PublicShopView;
import com.greenlink.core.application.shop.usecase.PublicShopUseCase;
import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderItem;
import com.greenlink.core.domain.shop.model.ShopProduct;
import com.greenlink.core.domain.shop.port.RateLimiter;
import com.greenlink.core.domain.shop.port.ShopOrderRepository;
import com.greenlink.core.domain.shop.port.ShopProductRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublicShopService implements PublicShopUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MAX_LINES = 10;
    private static final int MAX_QUANTITY = 99;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final int ORDER_LIMIT_PER_IP = 10;
    private static final int ORDER_LIMIT_PER_PHONE = 5;
    private static final int LOOKUP_LIMIT_PER_IP = 20;
    private static final String ORDER_NO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final ShopLookup shopLookup;
    private final ShopProductRepository productRepository;
    private final ShopOrderRepository orderRepository;
    private final RateLimiter rateLimiter;
    private final SecureRandom random = new SecureRandom();

    public PublicShopService(ShopLookup shopLookup, ShopProductRepository productRepository,
                             ShopOrderRepository orderRepository, RateLimiter rateLimiter) {
        this.shopLookup = shopLookup;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.rateLimiter = rateLimiter;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicShopView getShop(String slug) {
        Shop shop = shopLookup.findPublished(slug);
        return PublicShopView.of(shop, productRepository.findVisibleByShopId(shop.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProductView getProduct(String slug, Long productId) {
        Shop shop = shopLookup.findPublished(slug);
        return productRepository.findVisibleByIdAndShopId(productId, shop.getId())
                .map(PublicProductView::from)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional
    public PlacedShopOrderResult placeOrder(String slug, PlaceShopOrderCommand cmd, String clientIp) {
        Shop shop = shopLookup.findPublished(slug);

        if (cmd.honeypot() != null && !cmd.honeypot().isBlank()) {
            return new PlacedShopOrderResult(newOrderNo(), "PENDING");
        }

        String ordererPhone = phone(cmd.ordererPhone());
        String recipientPhone = phone(cmd.recipientPhone());
        if (!rateLimiter.tryAcquire("shop-order:ip:" + clientIp, ORDER_LIMIT_PER_IP, WINDOW)
                || !rateLimiter.tryAcquire("shop-order:phone:" + ordererPhone, ORDER_LIMIT_PER_PHONE, WINDOW)) {
            throw new BusinessException(ShopErrorCode.TOO_MANY_REQUESTS);
        }

        List<ShopOrderItem> items = priceItems(shop, cmd.items());
        ShopOrder.Contact contact = new ShopOrder.Contact(
                required(cmd.ordererName(), 50), ordererPhone,
                required(cmd.recipientName(), 50), recipientPhone,
                required(cmd.address(), 255), optional(cmd.addressDetail(), 255),
                deliveryDate(cmd.deliveryDate()), optional(cmd.deliveryTime(), 50),
                optional(cmd.eventType(), 20), optional(cmd.placeName(), 100),
                optional(cmd.ribbonLeft(), 100), optional(cmd.ribbonRight(), 200),
                optional(cmd.memo(), 500));

        ShopOrder saved = orderRepository.save(ShopOrder.place(shop, newUniqueOrderNo(), items, contact));
        return new PlacedShopOrderResult(saved.getOrderNo(), saved.getStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicOrderStatusView lookupOrder(String slug, String orderNo, String ordererPhone, String clientIp) {
        Shop shop = shopLookup.findPublished(slug);
        if (!rateLimiter.tryAcquire("shop-lookup:ip:" + clientIp, LOOKUP_LIMIT_PER_IP, Duration.ofMinutes(1))) {
            throw new BusinessException(ShopErrorCode.TOO_MANY_REQUESTS);
        }
        String normalizedPhone = digits(ordererPhone);
        String normalizedNo = orderNo == null ? "" : orderNo.trim().toUpperCase();
        return orderRepository.findByShopIdAndOrderNo(shop.getId(), normalizedNo)
                .filter(o -> o.getOrdererPhone().equals(normalizedPhone))
                .map(PublicOrderStatusView::from)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.ORDER_LOOKUP_FAILED));
    }

    private List<ShopOrderItem> priceItems(Shop shop, List<PlaceShopOrderCommand.Line> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (PlaceShopOrderCommand.Line line : lines) {
            if (line == null || line.productId() == null || line.quantity() < 1 || line.quantity() > MAX_QUANTITY) {
                throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
            }
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }
        if (quantities.size() > MAX_LINES || quantities.values().stream().anyMatch(q -> q > MAX_QUANTITY)) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
        Map<Long, ShopProduct> products = new LinkedHashMap<>();
        for (ShopProduct p : productRepository.findVisibleByIdInAndShopId(quantities.keySet(), shop.getId())) {
            products.put(p.getId(), p);
        }
        if (products.size() != quantities.size()) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
        return quantities.entrySet().stream()
                .map(e -> {
                    ShopProduct p = products.get(e.getKey());
                    return new ShopOrderItem(p.getId(), p.getName(), p.getPrice(), e.getValue());
                })
                .toList();
    }

    private String newUniqueOrderNo() {
        for (int i = 0; i < 5; i++) {
            String no = newOrderNo();
            if (!orderRepository.existsByOrderNo(no)) return no;
        }
        throw new IllegalStateException("주문번호 생성에 실패했습니다.");
    }

    private String newOrderNo() {
        StringBuilder sb = new StringBuilder("SO-")
                .append(LocalDate.now(KST).format(DateTimeFormatter.BASIC_ISO_DATE)).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(ORDER_NO_CHARS.charAt(random.nextInt(ORDER_NO_CHARS.length())));
        }
        return sb.toString();
    }

    private static String required(String value, int max) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty() || v.length() > max) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
        return v;
    }

    private static String optional(String value, int max) {
        String v = value == null ? "" : value.trim();
        if (v.length() > max) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
        return v.isEmpty() ? null : v;
    }

    private static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private static String phone(String value) {
        String d = digits(value);
        if (d.length() < 9 || d.length() > 11) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
        return d;
    }

    private static String deliveryDate(String value) {
        try {
            LocalDate date = LocalDate.parse(required(value, 10));
            if (date.isBefore(LocalDate.now(KST))) {
                throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
            }
            return date.toString();
        } catch (DateTimeParseException e) {
            throw new BusinessException(ShopErrorCode.INVALID_ORDER_REQUEST);
        }
    }
}
