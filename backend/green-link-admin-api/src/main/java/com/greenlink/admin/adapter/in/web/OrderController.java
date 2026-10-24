package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateOrderStatusRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.orderhistory.dto.OrderHistoryResult;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrdersUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderStatusUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderUseCase;
import com.greenlink.core.application.orderhistory.usecase.GetOrderHistoryUseCase;
import com.greenlink.core.application.sms.usecase.SendDeliveryMmsUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final GetOrderHistoryUseCase getOrderHistoryUseCase;
    private final SendDeliveryMmsUseCase sendDeliveryMmsUseCase;

    public OrderController(GetOrdersUseCase getOrdersUseCase,
                           GetOrderUseCase getOrderUseCase,
                           CreateOrderUseCase createOrderUseCase,
                           UpdateOrderUseCase updateOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase,
                           GetOrderHistoryUseCase getOrderHistoryUseCase,
                           SendDeliveryMmsUseCase sendDeliveryMmsUseCase) {
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.getOrderHistoryUseCase = getOrderHistoryUseCase;
        this.sendDeliveryMmsUseCase = sendDeliveryMmsUseCase;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long resolvedVendorId = vendorId;
        Long resolvedOrderingCompanyId = orderingCompanyId;

        if (auth != null && auth.isAuthenticated()) {
            boolean isVendor = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));
            boolean isOrdering = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ORDERING"));
            if (isVendor) {
                resolvedVendorId = Long.parseLong(String.valueOf(auth.getPrincipal()));
            } else if (isOrdering) {
                if (auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token
                        && token.getDetails() instanceof Long cid) {
                    resolvedOrderingCompanyId = cid;
                }
            }
        }
        OrderSearchCondition condition = OrderSearchCondition.of(
                status, resolvedVendorId, orderNumber, productName, ordererName,
                deliveryDateFrom, deliveryDateTo, resolvedOrderingCompanyId);
        return ResponseEntity.ok(getOrdersUseCase.getOrdersPaged(condition, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResult> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(getOrderUseCase.getOrder(id));
    }

    @PostMapping
    public ResponseEntity<OrderResult> create(Authentication auth,
                                               @RequestBody @Valid CreateOrderRequest req) {
        Long orderingCompanyId = null;
        if (auth != null && auth.isAuthenticated()
                && auth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken token
                && token.getDetails() instanceof Long cid) {
            orderingCompanyId = cid;
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createOrderUseCase.createOrder(req.toCommand(orderingCompanyId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResult> update(@PathVariable Long id,
                                              @RequestBody @Valid UpdateOrderRequest req) {
        return ResponseEntity.ok(updateOrderUseCase.updateOrder(id, req.toCommand()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResult> updateStatus(@PathVariable Long id,
                                                    @RequestBody @Valid UpdateOrderStatusRequest req) {
        OrderResult result = updateOrderStatusUseCase.updateOrderStatus(id, req.action());
        if ("complete".equalsIgnoreCase(req.action())) {
            try {
                sendDeliveryMmsUseCase.send(id, req.photoUrls());
            } catch (Exception ignored) {
                // MMS 발송 실패는 배송완료 처리를 막지 않음
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderHistoryResult>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(getOrderHistoryUseCase.getHistory(id));
    }
}
