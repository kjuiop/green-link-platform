package com.greenlink.core.domain.order.model;

public class OrderItem {

    private static final int MAX_NAME_LENGTH = 100;

    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private long unitPrice;
    private int quantity;
    private long amount;
    private int sortOrder;

    private OrderItem() {}

    public static OrderItem create(Long productId, String productName, long unitPrice, int quantity) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (productName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("상품명은 " + MAX_NAME_LENGTH + "자 이하여야 합니다.");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("단가는 0 이상이어야 합니다.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        OrderItem item = new OrderItem();
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice;
        item.quantity = quantity;
        try {
            item.amount = Math.multiplyExact(unitPrice, (long) quantity);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("금액이 허용 범위를 초과했습니다.");
        }
        return item;
    }

    public static OrderItem of(Long id, Long orderId, Long productId, String productName,
                               long unitPrice, int quantity, long amount, int sortOrder) {
        OrderItem item = new OrderItem();
        item.id = id;
        item.orderId = orderId;
        item.productId = productId;
        item.productName = productName;
        item.unitPrice = unitPrice;
        item.quantity = quantity;
        item.amount = amount;
        item.sortOrder = sortOrder;
        return item;
    }

    public static OrderItem fromLegacy(Long orderId, Long productId, String productName, int quantity, long amount) {
        long unitPrice = quantity > 0 ? amount / quantity : amount;
        return of(null, orderId, productId, productName, unitPrice, quantity, amount, 0);
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public int getSortOrder() { return sortOrder; }
}
