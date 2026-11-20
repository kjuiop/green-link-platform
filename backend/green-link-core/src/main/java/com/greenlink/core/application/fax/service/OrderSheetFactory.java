package com.greenlink.core.application.fax.service;

import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.fax.model.OrderSheet;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OrderSheetFactory {

    private OrderSheetFactory() {}

    public static OrderSheet create(Order order, List<OrderItem> items, Company ordering, Company vendor) {
        String productSummary = items.stream()
                .map(i -> i.getProductName() + " × " + i.getQuantity())
                .collect(Collectors.joining(", "));
        String customerName = notBlank(order.getRecipientName()) ? order.getRecipientName() : order.getOrdererName();
        return new OrderSheet(
                order.getOrderNumber(),
                ordering != null ? ordering.getCompanyName() : null,
                ordering != null ? ordering.getPhone() : null,
                vendor != null ? vendor.getCompanyName() : null,
                vendor != null ? vendor.getPhone() : null,
                productSummary,
                order.getTotalAmount(),
                deliveryAddress(order),
                deliveryDateTime(order),
                customerName,
                order.getRecipientPhone(),
                order.getRibbonLeft(),
                order.getRibbonRight(),
                order.getRibbonExtras(),
                order.getEventInfo(),
                order.getVendorMemo());
    }

    static String deliveryAddress(Order o) {
        String addr = Stream.of(o.getAddress(), o.getAddressDetail()).filter(OrderSheetFactory::notBlank).collect(Collectors.joining(" "));
        String place = Stream.of(o.getPlaceName(), o.getRoomName()).filter(OrderSheetFactory::notBlank).collect(Collectors.joining(" "));
        return Stream.of(addr, place).filter(OrderSheetFactory::notBlank).collect(Collectors.joining(" / "));
    }

    static String deliveryDateTime(Order o) {
        if (!notBlank(o.getDeliveryDate())) return "";
        StringBuilder sb = new StringBuilder(o.getDeliveryDate());
        String type = o.getDeliveryTimeType();
        if ("3시간이내".equals(type)) {
            sb.append(" 3시간이내");
        } else if ("직접입력".equals(type) && notBlank(o.getDeliveryTimeCustom())) {
            sb.append(' ').append(o.getDeliveryTimeCustom());
        } else if (("까지".equals(type) || "예식".equals(type) || "행사".equals(type))
                && notBlank(o.getDeliveryTimeHour()) && notBlank(o.getDeliveryTimeMinute())) {
            sb.append(' ').append(o.getDeliveryTimeHour()).append(':').append(o.getDeliveryTimeMinute());
            sb.append("까지".equals(type) ? "까지" : " " + type);
        }
        return sb.toString();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
