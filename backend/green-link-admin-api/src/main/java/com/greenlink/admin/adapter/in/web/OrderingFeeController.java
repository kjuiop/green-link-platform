package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.UpdateOrderingFeeRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.OrderingFeeResult;
import com.greenlink.core.application.company.usecase.ManageOrderingFeeUseCase;
import com.greenlink.core.domain.company.model.OrderingFeeDiscountType;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ordering-fees")
public class OrderingFeeController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ManageOrderingFeeUseCase manageOrderingFeeUseCase;

    public OrderingFeeController(ManageOrderingFeeUseCase manageOrderingFeeUseCase) {
        this.manageOrderingFeeUseCase = manageOrderingFeeUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<OrderingFeeResult>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageOrderingFeeUseCase.getOrderingFees(
                keyword,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE))));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<OrderingFeeResult> update(
            @PathVariable Long companyId,
            @RequestBody @Valid UpdateOrderingFeeRequest req) {
        OrderingFeeDiscountType type;
        try {
            type = OrderingFeeDiscountType.valueOf(req.discountType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(manageOrderingFeeUseCase.updateOrderingFee(companyId, type, req.discountValue()));
    }
}
