package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.UpdateFaxVendorRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.fax.dto.FaxMessageResult;
import com.greenlink.core.application.fax.dto.FaxVendorResult;
import com.greenlink.core.application.fax.usecase.DispatchOrderFaxUseCase;
import com.greenlink.core.application.fax.usecase.ManageFaxUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fax")
public class FaxAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ManageFaxUseCase manageFaxUseCase;
    private final DispatchOrderFaxUseCase dispatchOrderFaxUseCase;

    public FaxAdminController(ManageFaxUseCase manageFaxUseCase, DispatchOrderFaxUseCase dispatchOrderFaxUseCase) {
        this.manageFaxUseCase = manageFaxUseCase;
        this.dispatchOrderFaxUseCase = dispatchOrderFaxUseCase;
    }

    @GetMapping("/vendors")
    public ResponseEntity<PageResult<FaxVendorResult>> vendors(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageFaxUseCase.getVendors(keyword, pageable(page, size)));
    }

    @PutMapping("/vendors/{companyId}")
    public ResponseEntity<FaxVendorResult> updateVendor(@PathVariable Long companyId,
                                                        @RequestBody @Valid UpdateFaxVendorRequest req) {
        return ResponseEntity.ok(manageFaxUseCase.updateVendor(companyId, req.faxEnabled(), req.faxNumber()));
    }

    @GetMapping("/messages")
    public ResponseEntity<PageResult<FaxMessageResult>> messages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageFaxUseCase.getMessages(status, keyword, pageable(page, size)));
    }

    @PostMapping("/orders/{orderId}/resend")
    public ResponseEntity<FaxMessageResult> resend(@PathVariable Long orderId) {
        return ResponseEntity.ok(dispatchOrderFaxUseCase.resend(orderId));
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }
}
