package com.greenlink.core.domain.orderhistory.model;

public class OrderHistory {

    private Long id;
    private Long orderId;
    private OrderHistoryAction action;
    private String detail;
    private String actorCompany;
    private String actorName;
    private Long actorAdminId;
    private String createdAt;

    private OrderHistory() {}

    public static OrderHistory create(Long orderId, OrderHistoryAction action, String detail,
                                      String actorCompany, String actorName, Long actorAdminId) {
        OrderHistory h = new OrderHistory();
        h.orderId = orderId;
        h.action = action;
        h.detail = detail;
        h.actorCompany = actorCompany;
        h.actorName = actorName;
        h.actorAdminId = actorAdminId;
        return h;
    }

    public static OrderHistory of(Long id, Long orderId, OrderHistoryAction action, String detail,
                                   String actorCompany, String actorName, Long actorAdminId, String createdAt) {
        OrderHistory h = new OrderHistory();
        h.id = id;
        h.orderId = orderId;
        h.action = action;
        h.detail = detail;
        h.actorCompany = actorCompany;
        h.actorName = actorName;
        h.actorAdminId = actorAdminId;
        h.createdAt = createdAt;
        return h;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public OrderHistoryAction getAction() { return action; }
    public String getDetail() { return detail; }
    public String getActorCompany() { return actorCompany; }
    public String getActorName() { return actorName; }
    public Long getActorAdminId() { return actorAdminId; }
    public String getCreatedAt() { return createdAt; }
}
