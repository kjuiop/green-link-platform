package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateOrderRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.coupang.dto.CoupangOrderResult;
import com.greenlink.core.application.coupang.dto.CoupangSyncResult;
import com.greenlink.core.application.coupang.usecase.CollectCoupangOrdersUseCase;
import com.greenlink.core.application.coupang.usecase.ProcessCoupangOrderUseCase;
import com.greenlink.core.application.coupang.usecase.QueryCoupangOrdersUseCase;
import com.greenlink.core.application.order.dto.CreateOrderCommand;
import com.greenlink.core.application.order.dto.OrderResult;
import com.greenlink.core.domain.coupang.model.CoupangOrderStatus;
import com.greenlink.core.domain.coupang.model.CoupangTriggerType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupang/orders")
public class CoupangOrderController {

    private final QueryCoupangOrdersUseCase queryUseCase;
    private final CollectCoupangOrdersUseCase collectUseCase;
    private final ProcessCoupangOrderUseCase processUseCase;
    private final CoupangAccess access;

    public CoupangOrderController(QueryCoupangOrdersUseCase queryUseCase,
                                  CollectCoupangOrdersUseCase collectUseCase,
                                  ProcessCoupangOrderUseCase processUseCase,
                                  CoupangAccess access) {
        this.queryUseCase = queryUseCase;
        this.collectUseCase = collectUseCase;
        this.processUseCase = processUseCase;
        this.access = access;
    }

    @GetMapping
    public ResponseEntity<PageResult<CoupangOrderResult>> list(Authentication auth,
                                                               @RequestParam(required = false) Long companyId,
                                                               @RequestParam(required = false) CoupangOrderStatus status,
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
    public ResponseEntity<CoupangOrderResult> get(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(queryUseCase.get(access.callerCompanyId(auth), id));
    }

    @PostMapping("/collect")
    public ResponseEntity<CoupangSyncResult> collect(Authentication auth,
                                                     @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(collectUseCase.collect(access.targetCompanyId(auth, companyId),
                CoupangTriggerType.MANUAL));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OrderResult> approve(Authentication auth, @PathVariable Long id,
                                               @RequestBody @Valid CreateOrderRequest req) {
        Long callerCompanyId = access.callerCompanyId(auth);
        boolean orderingCaller = callerCompanyId != null;
        OrderResult created = processUseCase.approve(callerCompanyId, id, cid -> {
            CreateOrderCommand cmd = req.toCommand(cid);
            return orderingCaller ? cmd.withVendorAmount(null) : cmd;
        });
        return ResponseEntity.status(HttpStatus.CREATED).body(orderingCaller ? created.toOrderingView() : created);
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<CoupangOrderResult> hold(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(processUseCase.hold(access.callerCompanyId(auth), id));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<CoupangOrderResult> release(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(processUseCase.release(access.callerCompanyId(auth), id));
    }
}
