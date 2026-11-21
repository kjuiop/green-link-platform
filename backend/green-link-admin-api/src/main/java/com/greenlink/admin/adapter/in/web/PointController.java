package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.ChargePointRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.point.dto.CompanyPointResult;
import com.greenlink.core.application.point.dto.PointTransactionResult;
import com.greenlink.core.application.point.usecase.ChargePointUseCase;
import com.greenlink.core.application.point.usecase.GetAdminPointListUseCase;
import com.greenlink.core.application.point.usecase.GetMyPointUseCase;
import com.greenlink.core.application.point.usecase.GetPointTransactionsUseCase;
import com.greenlink.core.application.taxinvoice.dto.TaxInvoiceResult;
import com.greenlink.core.application.taxinvoice.usecase.GetTaxInvoiceStatusUseCase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PointController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ChargePointUseCase chargePointUseCase;
    private final GetAdminPointListUseCase getAdminPointListUseCase;
    private final GetMyPointUseCase getMyPointUseCase;
    private final GetPointTransactionsUseCase getPointTransactionsUseCase;
    private final GetTaxInvoiceStatusUseCase getTaxInvoiceStatusUseCase;

    public PointController(ChargePointUseCase chargePointUseCase,
                           GetAdminPointListUseCase getAdminPointListUseCase,
                           GetMyPointUseCase getMyPointUseCase,
                           GetPointTransactionsUseCase getPointTransactionsUseCase,
                           GetTaxInvoiceStatusUseCase getTaxInvoiceStatusUseCase) {
        this.chargePointUseCase = chargePointUseCase;
        this.getAdminPointListUseCase = getAdminPointListUseCase;
        this.getMyPointUseCase = getMyPointUseCase;
        this.getPointTransactionsUseCase = getPointTransactionsUseCase;
        this.getTaxInvoiceStatusUseCase = getTaxInvoiceStatusUseCase;
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
        PageResult<PointTransactionResult> result = getPointTransactionsUseCase.getTransactions(
                companyId, pageRequest(page, size));
        List<Long> chargeIds = result.content().stream()
                .filter(t -> "CHARGE".equals(t.type()))
                .map(PointTransactionResult::id)
                .toList();
        Map<Long, TaxInvoiceResult> invoices = getTaxInvoiceStatusUseCase.getByPointTransactionIds(chargeIds);
        List<PointTransactionResult> enriched = result.content().stream()
                .map(t -> {
                    TaxInvoiceResult inv = invoices.get(t.id());
                    return inv == null ? t : t.withTaxInvoice(inv.status(), inv.failReason());
                })
                .toList();
        return ResponseEntity.ok(new PageResult<>(enriched, result.page(), result.size(),
                result.totalElements(), result.totalPages(), result.totalAmount()));
    }

    @GetMapping("/api/points/my")
    public ResponseEntity<CompanyPointResult> getMyPoint(
            @RequestParam(required = false) Long companyId,
            Authentication auth) {
        return ResponseEntity.ok(getMyPointUseCase.getMyPoint(resolveCompanyId(auth, companyId)));
    }

    @GetMapping("/api/points/my/transactions")
    public ResponseEntity<PageResult<PointTransactionResult>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long companyId,
            Authentication auth) {
        return ResponseEntity.ok(getPointTransactionsUseCase.getTransactions(
                resolveCompanyId(auth, companyId), pageRequest(page, size)));
    }

    private Long resolveCompanyId(Authentication auth, Long paramCompanyId) {
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            if (paramCompanyId == null) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "companyId는 필수입니다.");
            }
            return paramCompanyId;
        }
        if (auth.getDetails() instanceof Long cid) {
            return cid;
        }
        throw new org.springframework.security.access.AccessDeniedException("회사 정보가 없습니다.");
    }

    private static PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("id").descending());
    }
}
