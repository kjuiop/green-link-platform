package com.greenlink.core.application.order.service;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.application.order.dto.UpdateOrderCommand;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrdersUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderStatusUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderUseCase;
import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.model.OrderStatus;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.orderhistory.model.OrderHistoryAction;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import com.greenlink.core.domain.referencephoto.port.ReferencePhotoRepository;
import com.greenlink.core.domain.sms.model.SmsMessage;
import com.greenlink.core.domain.sms.model.SmsStatus;
import com.greenlink.core.domain.sms.port.SmsMessageRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import com.greenlink.core.application.order.event.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService implements CreateOrderUseCase, GetOrderUseCase,
        GetOrdersUseCase, UpdateOrderUseCase, UpdateOrderStatusUseCase {

    private static final Map<OrderStatus, String> STATUS_LABEL = Map.of(
            OrderStatus.PENDING,   "발주대기",
            OrderStatus.ACCEPTED,  "수주완료",
            OrderStatus.DELIVERED, "배송완료",
            OrderStatus.REJECTED,  "수주거절",
            OrderStatus.CANCELLED, "주문취소"
    );

    private final OrderRepository orderRepository;
    private final PhotoRepository photoRepository;
    private final ReceiptConfirmationRepository receiptConfirmationRepository;
    private final RecordOrderHistoryUseCase recordOrderHistoryUseCase;
    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final ReferencePhotoRepository referencePhotoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, PhotoRepository photoRepository,
                        ReceiptConfirmationRepository receiptConfirmationRepository,
                        RecordOrderHistoryUseCase recordOrderHistoryUseCase,
                        AdminRepository adminRepository, CompanyRepository companyRepository,
                        SmsMessageRepository smsMessageRepository,
                        ReferencePhotoRepository referencePhotoRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.photoRepository = photoRepository;
        this.receiptConfirmationRepository = receiptConfirmationRepository;
        this.recordOrderHistoryUseCase = recordOrderHistoryUseCase;
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.referencePhotoRepository = referencePhotoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResult createOrder(CreateOrderCommand cmd) {
        String orderNumber = "DF-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-"
                + String.format("%04d", System.nanoTime() % 10000);

        Order order = Order.create(
                cmd.vendorId(),
                cmd.orderingCompanyId(),
                orderNumber,
                cmd.productName(),
                cmd.quantity(),
                cmd.amount(),
                cmd.extraCost(),
                cmd.deliveryDate(),
                cmd.deliveryTimeType(),
                cmd.deliveryTimeHour(),
                cmd.deliveryTimeMinute(),
                cmd.deliveryTimeCustom(),
                cmd.address(),
                cmd.addressDetail(),
                cmd.eventType(),
                cmd.placeName(),
                cmd.roomName(),
                cmd.eventInfo(),
                cmd.ribbonLeft(),
                cmd.ribbonRight(),
                cmd.ordererName(),
                cmd.ordererPhone(),
                cmd.recipientName(),
                cmd.recipientPhone(),
                cmd.sendPhotoToOrderer(),
                cmd.sendPhotoToRecipient(),
                cmd.messageSchedule(),
                cmd.scheduledMessageHour(),
                cmd.scheduledMessageMinute(),
                cmd.adminMemo(),
                cmd.vendorMemo()
        );

        Order saved = orderRepository.save(order);
        recordOrderHistoryUseCase.record(saved.getId(), OrderHistoryAction.CREATED,
                "발주 등록: " + saved.getOrderNumber());

        String vendorPhone = resolveVendorPhone(saved.getVendorId());
        if (vendorPhone != null && !vendorPhone.isBlank()) {
            eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId(), vendorPhone));
        }

        return toResult(saved);
    }

    @Override
    public OrderResult getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        int refCount = referencePhotoRepository.countByOrderIds(List.of(id)).getOrDefault(id, 0L).intValue();
        return toResult(order, null, null, null, null, refCount);
    }

    @Override
    public List<OrderResult> getOrders(OrderSearchCondition condition) {
        List<Order> orders = orderRepository.search(condition);
        Map<Long, VendorInfo> infoMap = buildVendorInfoMap();
        Map<Long, String> orderingNameMap = buildOrderingCompanyNameMap();
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, String> msgStatusMap = buildMessageStatusMap(orderIds);
        Map<Long, Long> refCountMap = orderIds.isEmpty() ? Map.of() : referencePhotoRepository.countByOrderIds(orderIds);
        return orders.stream()
                .map(o -> {
                    VendorInfo v = infoMap.get(o.getVendorId());
                    String orderingName = o.getOrderingCompanyId() != null ? orderingNameMap.get(o.getOrderingCompanyId()) : null;
                    int refCount = refCountMap.getOrDefault(o.getId(), 0L).intValue();
                    return toResult(o, v != null ? v.name() : null, v != null ? v.phone() : null, orderingName, msgStatusMap.get(o.getId()), refCount);
                })
                .toList();
    }

    @Override
    public PageResult<OrderResult> getOrdersPaged(OrderSearchCondition condition, Pageable pageable) {
        Page<Order> page = orderRepository.searchPaged(condition, pageable);
        Map<Long, VendorInfo> infoMap = buildVendorInfoMap();
        Map<Long, String> orderingNameMap = buildOrderingCompanyNameMap();
        List<Long> orderIds = page.getContent().stream().map(Order::getId).toList();
        Map<Long, String> msgStatusMap = buildMessageStatusMap(orderIds);
        Map<Long, Long> refCountMap = orderIds.isEmpty() ? Map.of() : referencePhotoRepository.countByOrderIds(orderIds);
        Page<OrderResult> resultPage = page.map(o -> {
            VendorInfo v = infoMap.get(o.getVendorId());
            String orderingName = o.getOrderingCompanyId() != null ? orderingNameMap.get(o.getOrderingCompanyId()) : null;
            int refCount = refCountMap.getOrDefault(o.getId(), 0L).intValue();
            return toResult(o, v != null ? v.name() : null, v != null ? v.phone() : null, orderingName, msgStatusMap.get(o.getId()), refCount);
        });
        return PageResult.of(resultPage);
    }

    private record VendorInfo(String name, String phone) {}

    private Map<Long, VendorInfo> buildVendorInfoMap() {
        List<Admin> adminsWithCompany = adminRepository.findAllVendors();
        List<Long> companyIds = adminsWithCompany.stream()
                .map(Admin::getCompanyId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, Company> companyById = companyRepository.findAllByIdIn(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, c -> c));
        Map<Long, VendorInfo> result = new HashMap<>();
        for (Admin a : adminsWithCompany) {
            if (a.getCompanyId() != null) {
                Company c = companyById.get(a.getCompanyId());
                // ORDERING 타입 회사는 수주사(vendor) 정보 맵에서 제외
                if (c != null && c.getType() != null) {
                    switch (c.getType()) {
                        case VENDOR, BOTH -> result.put(a.getId(),
                                new VendorInfo(c.getCompanyName(), c.getPhone()));
                        default -> { /* ORDERING은 제외 */ }
                    }
                }
            }
        }
        return result;
    }

    private Map<Long, String> buildOrderingCompanyNameMap() {
        return companyRepository.findAllByType(com.greenlink.core.domain.company.model.CompanyType.ORDERING).stream()
                .collect(Collectors.toMap(Company::getId, Company::getCompanyName));
    }

    private String resolveOrderingCompanyName(Long orderingCompanyId) {
        if (orderingCompanyId == null) return null;
        return companyRepository.findById(orderingCompanyId).map(Company::getCompanyName).orElse(null);
    }

    private OrderResult toResult(Order order, String vendorName, String vendorPhone, String orderingCompanyName, String messageStatus, int referencePhotoCount) {
        return new OrderResult(
                order.getId(),
                order.getOrderNumber(),
                order.getVendorId(),
                vendorName,
                vendorPhone,
                order.getOrderingCompanyId(),
                orderingCompanyName,
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getProductName(),
                order.getQuantity(),
                order.getAmount(),
                order.getExtraCost(),
                order.getDeliveryDate(),
                order.getDeliveryTimeType(),
                order.getDeliveryTimeHour(),
                order.getDeliveryTimeMinute(),
                order.getDeliveryTimeCustom(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getEventType(),
                order.getPlaceName(),
                order.getRoomName(),
                order.getEventInfo(),
                order.getRibbonLeft(),
                order.getRibbonRight(),
                order.getOrdererName(),
                order.getOrdererPhone(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.isSendPhotoToOrderer(),
                order.isSendPhotoToRecipient(),
                order.getMessageSchedule(),
                order.getScheduledMessageHour(),
                order.getScheduledMessageMinute(),
                order.getAdminMemo(),
                order.getVendorMemo(),
                order.getCompletedAt(),
                order.getCreatedAt(),
                messageStatus,
                referencePhotoCount
        );
    }

    private Map<Long, String> buildMessageStatusMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) return Map.of();
        List<SmsMessage> messages = smsMessageRepository.findAllByOrderIdIn(orderIds);
        Map<Long, List<SmsMessage>> byOrder = messages.stream()
                .collect(Collectors.groupingBy(SmsMessage::getOrderId));
        Map<Long, String> result = new HashMap<>();
        for (Long id : orderIds) {
            List<SmsMessage> msgs = byOrder.getOrDefault(id, List.of());
            if (msgs.isEmpty()) {
                result.put(id, "NONE");
            } else if (msgs.stream().anyMatch(m -> m.getStatus() == SmsStatus.SUCCESS)) {
                result.put(id, "SENT");
            } else {
                result.put(id, "FAIL");
            }
        }
        return result;
    }

    @Override
    @Transactional
    public OrderResult updateOrder(Long id, UpdateOrderCommand cmd) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateInfo(
                cmd.vendorId(),
                cmd.productName(),
                cmd.quantity(),
                cmd.amount(),
                cmd.extraCost(),
                cmd.deliveryDate(),
                cmd.deliveryTimeType(),
                cmd.deliveryTimeHour(),
                cmd.deliveryTimeMinute(),
                cmd.deliveryTimeCustom(),
                cmd.address(),
                cmd.addressDetail(),
                cmd.eventType(),
                cmd.placeName(),
                cmd.roomName(),
                cmd.eventInfo(),
                cmd.ribbonLeft(),
                cmd.ribbonRight(),
                cmd.ordererName(),
                cmd.ordererPhone(),
                cmd.recipientName(),
                cmd.recipientPhone(),
                cmd.sendPhotoToOrderer(),
                cmd.sendPhotoToRecipient(),
                cmd.messageSchedule(),
                cmd.scheduledMessageHour(),
                cmd.scheduledMessageMinute(),
                cmd.adminMemo(),
                cmd.vendorMemo()
        );

        Order saved = orderRepository.save(order);
        recordOrderHistoryUseCase.record(saved.getId(), OrderHistoryAction.UPDATED, "발주 정보 수정");
        return toResult(saved);
    }

    @Override
    @Transactional
    public OrderResult updateOrderStatus(Long id, String action) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        switch (action.toLowerCase()) {
            case "accept" -> order.accept();
            case "reject" -> order.reject();
            case "cancel" -> order.cancel();
            case "complete" -> {
                if (!receiptConfirmationRepository.existsWithPhotosByOrderId(id)) {
                    throw new BusinessException(OrderErrorCode.SITE_PHOTO_REQUIRED);
                }
                order.complete(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            default -> throw new BusinessException(OrderErrorCode.INVALID_ORDER_ACTION);
        }

        Order saved = orderRepository.save(order);
        recordOrderHistoryUseCase.record(saved.getId(), OrderHistoryAction.STATUS_CHANGED,
                "상태 변경: " + STATUS_LABEL.getOrDefault(saved.getStatus(), saved.getStatus().name()));
        return toResult(saved);
    }

    private String resolveVendorName(Long vendorId) {
        if (vendorId == null) return null;
        return adminRepository.findById(vendorId)
                .map(admin -> admin.getCompanyId() == null ? null :
                        companyRepository.findById(admin.getCompanyId())
                                .map(Company::getCompanyName)
                                .orElse(null))
                .orElse(null);
    }

    private String resolveVendorPhone(Long vendorId) {
        if (vendorId == null) return null;
        return adminRepository.findById(vendorId)
                .map(admin -> admin.getCompanyId() == null ? null :
                        companyRepository.findById(admin.getCompanyId())
                                .map(Company::getPhone)
                                .orElse(null))
                .orElse(null);
    }

    private OrderResult toResult(Order order) {
        List<SmsMessage> msgs = smsMessageRepository.findAllByOrderId(order.getId());
        String messageStatus;
        if (msgs.isEmpty()) {
            messageStatus = "NONE";
        } else if (msgs.stream().anyMatch(m -> m.getStatus() == SmsStatus.SUCCESS)) {
            messageStatus = "SENT";
        } else {
            messageStatus = "FAIL";
        }
        int refCount = referencePhotoRepository.countByOrderIds(List.of(order.getId()))
                .getOrDefault(order.getId(), 0L).intValue();
        return toResult(order,
                resolveVendorName(order.getVendorId()),
                resolveVendorPhone(order.getVendorId()),
                resolveOrderingCompanyName(order.getOrderingCompanyId()),
                messageStatus,
                refCount);
    }
}
