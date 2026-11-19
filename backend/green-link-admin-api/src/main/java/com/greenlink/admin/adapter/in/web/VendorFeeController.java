package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.UpdateVendorFeeRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.company.dto.VendorFeeResult;
import com.greenlink.core.application.company.usecase.ManageVendorFeeUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vendor-fees")
public class VendorFeeController {

    private final ManageVendorFeeUseCase manageVendorFeeUseCase;

    public VendorFeeController(ManageVendorFeeUseCase manageVendorFeeUseCase) {
        this.manageVendorFeeUseCase = manageVendorFeeUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<VendorFeeResult>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(manageVendorFeeUseCase.getVendorFees(keyword, PageRequest.of(page, size)));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<VendorFeeResult> update(@PathVariable Long companyId,
                                                  @RequestBody @Valid UpdateVendorFeeRequest req) {
        return ResponseEntity.ok(manageVendorFeeUseCase.updateVendorFee(companyId, req.vendorFee()));
    }
}
