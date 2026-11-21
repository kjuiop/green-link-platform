package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.customerorder.dto.CustomerOrderResult;
import com.greenlink.core.application.customerorder.usecase.ProcessCustomerOrderUseCase;
import com.greenlink.core.application.customerorder.usecase.QueryCustomerOrdersUseCase;
import com.greenlink.core.domain.customerorder.model.CustomerOrderStatus;
import com.greenlink.core.domain.customerorder.model.OrderChannel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/customer-orders")
public class CustomerOrderController {

    private final QueryCustomerOrdersUseCase queryUseCase;
    private final ProcessCustomerOrderUseCase processUseCase;
    private final CoupangAccess access;

    public CustomerOrderController(QueryCustomerOrdersUseCase queryUseCase,
                                   ProcessCustomerOrderUseCase processUseCase,
                                   CoupangAccess access) {
        this.queryUseCase = queryUseCase;
        this.processUseCase = processUseCase;
        this.access = access;
    }

    @GetMapping
    public ResponseEntity<PageResult<CustomerOrderResult>> list(
            Authentication auth,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) OrderChannel channel,
            @RequestParam(required = false) CustomerOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryUseCase.list(
                access.callerCompanyId(auth), companyId, channel, status, page, size));
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Long>> pendingCount(
            Authentication auth,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) OrderChannel channel) {
        return ResponseEntity.ok(Map.of("count",
                queryUseCase.pendingCount(access.callerCompanyId(auth), companyId, channel)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderResult> get(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(queryUseCase.get(access.callerCompanyId(auth), id));
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<CustomerOrderResult> hold(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(processUseCase.hold(access.callerCompanyId(auth), id));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<CustomerOrderResult> release(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(processUseCase.release(access.callerCompanyId(auth), id));
    }
}
