package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.RefundReasonRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateOrderStatusRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.orderhistory.dto.OrderHistoryResult;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.order.usecase.DeleteOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrdersUseCase;
import com.greenlink.core.application.order.usecase.RefundOrderUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderStatusUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderUseCase;
import com.greenlink.core.application.orderhistory.usecase.GetOrderHistoryUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryAlimtalkUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryMmsUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final RefundOrderUseCase refundOrderUseCase;
    private final DeleteOrderUseCase deleteOrderUseCase;
    private final GetOrderHistoryUseCase getOrderHistoryUseCase;
    private final SendDeliveryMmsUseCase sendDeliveryMmsUseCase;
    private final SendDeliveryAlimtalkUseCase sendDeliveryAlimtalkUseCase;

    public OrderController(GetOrdersUseCase getOrdersUseCase,
                           GetOrderUseCase getOrderUseCase,
                           CreateOrderUseCase createOrderUseCase,
                           UpdateOrderUseCase updateOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase,
                           RefundOrderUseCase refundOrderUseCase,
                           DeleteOrderUseCase deleteOrderUseCase,
                           GetOrderHistoryUseCase getOrderHistoryUseCase,
                           SendDeliveryMmsUseCase sendDeliveryMmsUseCase,
                           SendDeliveryAlimtalkUseCase sendDeliveryAlimtalkUseCase) {
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.refundOrderUseCase = refundOrderUseCase;
        this.deleteOrderUseCase = deleteOrderUseCase;
        this.getOrderHistoryUseCase = getOrderHistoryUseCase;
        this.sendDeliveryMmsUseCase = sendDeliveryMmsUseCase;
        this.sendDeliveryAlimtalkUseCase = sendDeliveryAlimtalkUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<OrderResult>> getAll(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long orderingCompanyId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String ordererName,
            @RequestParam(required = false) String deliveryDateFrom,
            @RequestParam(required = false) String deliveryDateTo,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String messageStatus,
            @RequestParam(required = false) String ribbonLeft,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) String orderingCompanyName,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String completedAtFrom,
            @RequestParam(required = false) String completedAtTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long resolvedVendorId = vendorId;
        Long resolvedOrderingCompanyId = orderingCompanyId;
        Boolean resolvedDeleted = null;
        boolean isVendor = false;
        boolean isSuperAdmin = false;

        if (auth != null && auth.isAuthenticated()) {
            isVendor = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
            boolean isOrdering = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ORDERING"));
            isSuperAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            if (isVendor) {
                resolvedVendorId = Long.parseLong(String.valueOf(auth.getPrincipal()));
            } else if (isOrdering) {
                if (auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token
                        && token.getDetails() instanceof Long cid) {
                    resolvedOrderingCompanyId = cid;
                }
            }
            if (isSuperAdmin) {
                resolvedDeleted = deleted;
            }
        }
        String resolvedCompletedAtFrom = isSuperAdmin ? completedAtFrom : null;
        String resolvedCompletedAtTo = isSuperAdmin ? completedAtTo : null;
        OrderSearchCondition condition = OrderSearchCondition.of(
                status, resolvedVendorId, orderNumber, productName, ordererName,
                deliveryDateFrom, deliveryDateTo, resolvedOrderingCompanyId,
                receiverName, messageStatus, ribbonLeft, resolvedDeleted,
                address, recipientName, vendorName, orderingCompanyName, sortBy, sortDir,
                resolvedCompletedAtFrom, resolvedCompletedAtTo);
        PageResult<OrderResult> result = isVendor
                ? getOrdersUseCase.getVendorOrdersPaged(condition, PageRequest.of(page, size))
                : getOrdersUseCase.getOrdersPaged(condition, PageRequest.of(page, size));
        if (isVendor) {
            List<OrderResult> masked = result.content().stream().map(OrderController::maskPhones).toList();
            result = new PageResult<>(masked, result.page(), result.size(), result.totalElements(), result.totalPages(), result.totalAmount());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResult> getOne(@PathVariable Long id, Authentication auth) {
        boolean isVendor = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        OrderResult result = getOrderUseCase.getOrder(id);
        return ResponseEntity.ok(isVendor ? maskPhones(result).toVendorView() : result);
    }

    @PostMapping
    public ResponseEntity<OrderResult> create(Authentication auth,
                                               @RequestBody @Valid CreateOrderRequest req) {
        Long orderingCompanyId = req.orderingCompanyId();
        if (auth != null && auth.isAuthenticated()
                && auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long cid) {
            orderingCompanyId = cid;
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createOrderUseCase.createOrder(req.toCommand(orderingCompanyId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResult> update(Authentication auth,
                                              @PathVariable Long id,
                                              @RequestBody @Valid UpdateOrderRequest req) {
        if (auth != null && auth.isAuthenticated()) {
            boolean isVendor = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
            if (isVendor) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            boolean isOrdering = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ORDERING"));
            if (isOrdering) {
                OrderResult existing = getOrderUseCase.getOrder(id);
                Long callerCompanyId = (auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token
                        && token.getDetails() instanceof Long cid) ? cid : null;
                if (callerCompanyId == null || !callerCompanyId.equals(existing.orderingCompanyId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }
        return ResponseEntity.ok(updateOrderUseCase.updateOrder(id, req.toCommand()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResult> updateStatus(Authentication auth,
                                                    @PathVariable Long id,
                                                    @RequestBody @Valid UpdateOrderStatusRequest req) {
        OrderResult result = updateOrderStatusUseCase.updateOrderStatus(id, req.action());
        if ("complete".equalsIgnoreCase(req.action())) {
            try {
                sendDeliveryMmsUseCase.send(id, req.photoUrls());
            } catch (Exception ignored) {
                // MMS 발송 실패는 배송완료 처리를 막지 않음
            }
            try {
                sendDeliveryAlimtalkUseCase.send(id);
            } catch (Exception ignored) {
                // 알림톡 발송 실패는 배송완료 처리를 막지 않음
            }
        }
        boolean isVendor = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        return ResponseEntity.ok(isVendor ? result.toVendorView() : result);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderHistoryResult>> getHistory(@PathVariable Long id, Authentication auth) {
        boolean isVendor = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
        List<OrderHistoryResult> history = getOrderHistoryUseCase.getHistory(id);
        if (isVendor) {
            history = history.stream()
                    .map(h -> new OrderHistoryResult(h.id(), h.orderId(), h.action(),
                            hideFeeAmounts(maskPhone(h.detail())), h.actorCompany(), h.actorName(), h.actorAdminId(), h.createdAt()))
                    .toList();
        }
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        if (!hasRole(auth, "ROLE_SUPER_ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        deleteOrderUseCase.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/refund-request")
    public ResponseEntity<OrderResult> requestRefund(Authentication auth, @PathVariable Long id,
                                                     @RequestBody @Valid RefundReasonRequest req) {
        boolean superAdmin = hasRole(auth, "ROLE_SUPER_ADMIN");
        if (!superAdmin) {
            if (!hasRole(auth, "ROLE_ORDERING")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            Long callerCompanyId = (auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token
                    && token.getDetails() instanceof Long cid) ? cid : null;
            if (callerCompanyId == null || !callerCompanyId.equals(getOrderUseCase.getOrder(id).orderingCompanyId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(refundOrderUseCase.requestRefund(id, req.reason(), superAdmin));
    }

    @PostMapping("/{id}/refund-approve")
    public ResponseEntity<OrderResult> approveRefund(Authentication auth, @PathVariable Long id) {
        boolean superAdmin = hasRole(auth, "ROLE_SUPER_ADMIN");
        if (!superAdmin && !isAssignedVendor(auth, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(refundOrderUseCase.approveRefund(id, superAdmin));
    }

    @PostMapping("/{id}/refund-reject")
    public ResponseEntity<OrderResult> rejectRefund(Authentication auth, @PathVariable Long id,
                                                    @RequestBody @Valid RefundReasonRequest req) {
        boolean superAdmin = hasRole(auth, "ROLE_SUPER_ADMIN");
        if (!superAdmin && !isAssignedVendor(auth, id)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(refundOrderUseCase.rejectRefund(id, req.reason(), superAdmin));
    }

    private boolean isAssignedVendor(Authentication auth, Long orderId) {
        if (!hasRole(auth, "ROLE_VENDOR")) return false;
        Long vendorId = getOrderUseCase.getOrder(orderId).vendorId();
        return vendorId != null && vendorId.equals(Long.parseLong(String.valueOf(auth.getPrincipal())));
    }

    private static boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private static String hideFeeAmounts(String detail) {
        if (detail == null || detail.isBlank()) return detail;
        String filtered = Arrays.stream(detail.split("\\n"))
                .filter(line -> !line.startsWith("상품금액:") && !line.startsWith("발주금액:"))
                .map(line -> line.startsWith("상품:") ? line.replaceAll("\\s\\d+x(\\d+)", " x$1") : line)
                .collect(Collectors.joining("\n"));
        return filtered.isBlank() ? "금액 정보 변경" : filtered;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return phone;
        return phone.replaceAll("(\\d{2,3})-?(\\d{3,4})-?(\\d{4})", "$1-****-$3");
    }

    private static OrderResult maskPhones(OrderResult r) {
        return new OrderResult(
                r.id(), r.orderNumber(), r.vendorId(), r.vendorCompanyId(), r.vendorName(), r.vendorPhone(),
                r.orderingCompanyId(), r.orderingCompanyName(), r.orderingCompanyPhone(),
                r.status(), r.productId(), r.productName(), r.quantity(), r.amount(),
                r.vendorAmount(), r.extraCost(), r.totalAmount(), r.vendorFee(), r.items(), r.deliveryDate(), r.deliveryTimeType(), r.deliveryTimeHour(),
                r.deliveryTimeMinute(), r.deliveryTimeCustom(), r.address(), r.addressDetail(),
                r.eventType(), r.placeName(), r.roomName(), r.eventInfo(),
                r.ribbonLeft(), r.ribbonRight(), r.ribbonExtras(),
                r.ordererName(), maskPhone(r.ordererPhone()),
                r.recipientName(), r.recipientPhone(),
                r.sendPhotoToOrderer(), r.sendPhotoToRecipient(),
                r.messageSchedule(), r.scheduledMessageHour(), r.scheduledMessageMinute(),
                r.adminMemo(), r.vendorMemo(), r.acceptedAt(), r.completedAt(),
                r.createdAt(), r.messageStatus(), r.referencePhotoCount(),
                r.hasReceipt(), r.receiptReceiverName(), r.receiptReceiverRelation(),
                r.isDeleted(), r.messageSentAt(),
                r.createdByAdminId(), r.updatedByAdminId(),
                r.isDelayed(), r.photoToken(),
                r.refundReason(), r.refundRequestedAt(), r.refundRejectReason()
        );
    }
}
