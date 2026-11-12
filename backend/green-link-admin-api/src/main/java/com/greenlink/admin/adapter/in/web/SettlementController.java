package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.settlement.dto.AdminSettlementQuery;
import com.greenlink.core.application.settlement.dto.SettlementOrderResult;
import com.greenlink.core.application.settlement.dto.SettlementSummaryResult;
import com.greenlink.core.application.settlement.usecase.*;
import com.greenlink.core.domain.settlement.model.SettlementType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class SettlementController {

    private final GetAdminSettlementsUseCase getAdminSettlementsUseCase;
    private final GetAdminSettlementDetailUseCase getAdminSettlementDetailUseCase;
    private final CompleteSettlementUseCase completeSettlementUseCase;
    private final CancelSettlementUseCase cancelSettlementUseCase;
    private final GetMySettlementUseCase getMySettlementUseCase;
    private final ExportSettlementExcelUseCase exportSettlementExcelUseCase;

    public SettlementController(GetAdminSettlementsUseCase getAdminSettlementsUseCase,
                                GetAdminSettlementDetailUseCase getAdminSettlementDetailUseCase,
                                CompleteSettlementUseCase completeSettlementUseCase,
                                CancelSettlementUseCase cancelSettlementUseCase,
                                GetMySettlementUseCase getMySettlementUseCase,
                                ExportSettlementExcelUseCase exportSettlementExcelUseCase) {
        this.getAdminSettlementsUseCase = getAdminSettlementsUseCase;
        this.getAdminSettlementDetailUseCase = getAdminSettlementDetailUseCase;
        this.completeSettlementUseCase = completeSettlementUseCase;
        this.cancelSettlementUseCase = cancelSettlementUseCase;
        this.getMySettlementUseCase = getMySettlementUseCase;
        this.exportSettlementExcelUseCase = exportSettlementExcelUseCase;
    }

    @GetMapping("/api/admin/settlements")
    public ResponseEntity<List<SettlementSummaryResult>> getAdminSettlements(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam SettlementType type) {
        return ResponseEntity.ok(getAdminSettlementsUseCase.getAdminSettlements(
                new AdminSettlementQuery(fromDate, toDate, type)));
    }

    @GetMapping("/api/admin/settlements/{companyId}")
    public ResponseEntity<List<SettlementOrderResult>> getAdminSettlementDetail(
            @PathVariable Long companyId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam SettlementType type) {
        return ResponseEntity.ok(getAdminSettlementDetailUseCase.getAdminSettlementDetail(
                companyId, new AdminSettlementQuery(fromDate, toDate, type)));
    }

    @PostMapping("/api/admin/settlements/{companyId}/complete")
    public ResponseEntity<Void> complete(
            @PathVariable Long companyId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam SettlementType type,
            Authentication auth) {
        Long adminId = Long.parseLong(auth.getName());
        completeSettlementUseCase.completeSettlement(companyId, type, fromDate, toDate, adminId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/admin/settlements/{companyId}/complete")
    public ResponseEntity<Void> cancelComplete(
            @PathVariable Long companyId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam SettlementType type) {
        cancelSettlementUseCase.cancelSettlement(companyId, type, fromDate, toDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/settlements/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam SettlementType type,
            @RequestParam Long companyId) {
        List<SettlementOrderResult> orders = getAdminSettlementDetailUseCase.getAdminSettlementDetail(
                companyId, new AdminSettlementQuery(fromDate, toDate, type));
        byte[] data = exportSettlementExcelUseCase.export(orders);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("settlement_" + fromDate + "_" + toDate + ".xlsx", StandardCharsets.UTF_8)
                .build());
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/api/settlements/my")
    public ResponseEntity<List<SettlementOrderResult>> getMySettlement(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam SettlementType type,
            Authentication auth) {
        Long companyId = (Long) auth.getDetails();
        return ResponseEntity.ok(getMySettlementUseCase.getMySettlement(companyId, type, fromDate, toDate));
    }
}
