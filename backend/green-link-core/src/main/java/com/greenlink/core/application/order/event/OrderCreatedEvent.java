package com.greenlink.core.application.order.event;

public record OrderCreatedEvent(Long orderId, String vendorPhone, String vendorName, String orderingCompanyName, String notificationTitle) {}
