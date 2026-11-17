package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.ChargePointRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.point.dto.CompanyPointResult;
import com.greenlink.core.application.point.dto.PointTransactionResult;
import com.greenlink.core.application.point.usecase.ChargePointUseCase;
import com.greenlink.core.application.point.usecase.GetAdminPointListUseCase;
import com.greenlink.core.application.point.usecase.GetMyPointUseCase;
import com.greenlink.core.application.point.usecase.GetPointTransactionsUseCase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PointController {

    private final ChargePointUseCase chargePointUseCase;
    private final GetAdminPointListUseCase getAdminPointListUseCase;
    private final GetMyPointUseCase getMyPointUseCase;
    private final GetPointTransactionsUseCase getPointTransactionsUseCase;

    public PointController(ChargePointUseCase chargePointUseCase,
                           GetAdminPointListUseCase getAdminPointListUseCase,
                           GetMyPointUseCase getMyPointUseCase,
                           GetPointTransactionsUseCase getPointTransactionsUseCase) {
        this.chargePointUseCase = chargePointUseCase;
        this.getAdminPointListUseCase = getAdminPointListUseCase;
        this.getMyPointUseCase = getMyPointUseCase;
        this.getPointTransactionsUseCase = getPointTransactionsUseCase;
    }

    @GetMapping("/api/admin/points")
    public ResponseEntity<List<CompanyPointResult>> getAdminPointList() {
        return ResponseEntity.ok(getAdminPointListUseCase.getAdminPointList());
    }

    @PostMapping("/api/admin/points/{companyId}/charge")
    public ResponseEntity<Void> charge(@PathVariable Long companyId,
                                       @RequestBody ChargePointRequest req,
                                       Authentication auth) {
        Long adminId = Long.parseLong(auth.getName());
        chargePointUseCase.charge(companyId, req.amount(), req.memo(), adminId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/points/{companyId}/transactions")
    public ResponseEntity<PageResult<PointTransactionResult>> getAdminTransactions(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getPointTransactionsUseCase.getTransactions(
                companyId, PageRequest.of(page, size, Sort.by("id").descending())));
    }

    @GetMapping("/api/points/my")
    public ResponseEntity<CompanyPointResult> getMyPoint(Authentication auth) {
        if (!(auth.getDetails() instanceof Long companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(getMyPointUseCase.getMyPoint(companyId));
    }

    @GetMapping("/api/points/my/transactions")
    public ResponseEntity<PageResult<PointTransactionResult>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        if (!(auth.getDetails() instanceof Long companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(getPointTransactionsUseCase.getTransactions(
                companyId, PageRequest.of(page, size, Sort.by("id").descending())));
    }
}
