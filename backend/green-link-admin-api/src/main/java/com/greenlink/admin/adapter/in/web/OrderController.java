package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateOrderStatusRequest;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.application.order.usecase.CreateOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrderUseCase;
import com.greenlink.core.application.order.usecase.GetOrdersUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderStatusUseCase;
import com.greenlink.core.application.order.usecase.UpdateOrderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public OrderController(GetOrdersUseCase getOrdersUseCase,
                           GetOrderUseCase getOrderUseCase,
                           CreateOrderUseCase createOrderUseCase,
                           UpdateOrderUseCase updateOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.createOrderUseCase = createOrderUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }

    @GetMapping
    public ResponseEntity<List<OrderResult>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String ordererName,
            @RequestParam(required = false) String deliveryDateFrom,
            @RequestParam(required = false) String deliveryDateTo) {
        OrderSearchCondition condition = OrderSearchCondition.of(
                status, vendorId, orderNumber, productName, ordererName, deliveryDateFrom, deliveryDateTo);
        return ResponseEntity.ok(getOrdersUseCase.getOrders(condition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResult> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(getOrderUseCase.getOrder(id));
    }

    @PostMapping
    public ResponseEntity<OrderResult> create(@RequestBody @Valid CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createOrderUseCase.createOrder(req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResult> update(@PathVariable Long id,
                                              @RequestBody @Valid UpdateOrderRequest req) {
        return ResponseEntity.ok(updateOrderUseCase.updateOrder(id, req.toCommand()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResult> updateStatus(@PathVariable Long id,
                                                    @RequestBody @Valid UpdateOrderStatusRequest req) {
        return ResponseEntity.ok(updateOrderStatusUseCase.updateOrderStatus(id, req.action()));
    }
}
