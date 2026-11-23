package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateOrderRequest;
import com.greenlink.admin.adapter.in.web.dto.RejectShopOrderRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.application.shop.dto.ShopOrderResult;
import com.greenlink.core.application.shop.usecase.ProcessShopOrderUseCase;
import com.greenlink.core.application.shop.usecase.QueryShopOrdersUseCase;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop/orders")
public class ShopOrderController {

    private final QueryShopOrdersUseCase queryUseCase;
    private final ProcessShopOrderUseCase processUseCase;
    private final ShopAccess access;

    public ShopOrderController(QueryShopOrdersUseCase queryUseCase, ProcessShopOrderUseCase processUseCase,
                               ShopAccess access) {
        this.queryUseCase = queryUseCase;
        this.processUseCase = processUseCase;
        this.access = access;
    }

    @GetMapping
    public ResponseEntity<PageResult<ShopOrderResult>> list(Authentication auth,
                                                            @RequestParam(required = false) Long companyId,
                                                            @RequestParam(required = false) ShopOrderStatus status,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryUseCase.list(access.callerCompanyId(auth), companyId, status, page, size));
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> pendingCount(Authentication auth,
                                                          @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(Map.of("count", queryUseCase.pendingCount(access.callerCompanyId(auth), companyId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopOrderResult> get(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(queryUseCase.get(access.callerCompanyId(auth), id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OrderResult> approve(Authentication auth, @PathVariable Long id,
                                               @RequestBody @Valid CreateOrderRequest req) {
        Long callerCompanyId = access.callerCompanyId(auth);
        boolean orderingCaller = callerCompanyId != null;
        OrderResult created = processUseCase.approve(callerCompanyId, id, access.adminId(auth), cid -> {
            CreateOrderCommand cmd = req.toCommand(cid);
            return orderingCaller ? cmd.withVendorAmount(null) : cmd;
        });
        return ResponseEntity.status(HttpStatus.CREATED).body(orderingCaller ? created.toOrderingView() : created);
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<ShopOrderResult> hold(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(processUseCase.hold(access.callerCompanyId(auth), id));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ShopOrderResult> release(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(processUseCase.release(access.callerCompanyId(auth), id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ShopOrderResult> reject(Authentication auth, @PathVariable Long id,
                                                  @RequestBody(required = false) @Valid RejectShopOrderRequest req) {
        return ResponseEntity.ok(processUseCase.reject(access.callerCompanyId(auth), id,
                req == null ? null : req.reason()));
    }
}
