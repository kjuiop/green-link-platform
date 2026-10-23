package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateVendorRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateVendorRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateVendorStatusRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.usecase.CreateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorsUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorStatusUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final GetVendorsUseCase getVendorsUseCase;
    private final GetVendorUseCase getVendorUseCase;
    private final CreateVendorUseCase createVendorUseCase;
    private final UpdateVendorUseCase updateVendorUseCase;
    private final UpdateVendorStatusUseCase updateVendorStatusUseCase;

    public VendorController(GetVendorsUseCase getVendorsUseCase,
                            GetVendorUseCase getVendorUseCase,
                            CreateVendorUseCase createVendorUseCase,
                            UpdateVendorUseCase updateVendorUseCase,
                            UpdateVendorStatusUseCase updateVendorStatusUseCase) {
        this.getVendorsUseCase = getVendorsUseCase;
        this.getVendorUseCase = getVendorUseCase;
        this.createVendorUseCase = createVendorUseCase;
        this.updateVendorUseCase = updateVendorUseCase;
        this.updateVendorStatusUseCase = updateVendorStatusUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<VendorResult>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getVendorsUseCase.getVendorsPaged(status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResult> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(getVendorUseCase.getVendor(id));
    }

    @PostMapping
    public ResponseEntity<VendorResult> create(@RequestBody @Valid CreateVendorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createVendorUseCase.createVendor(req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorResult> update(@PathVariable Long id,
                                               @RequestBody @Valid UpdateVendorRequest req) {
        return ResponseEntity.ok(updateVendorUseCase.updateVendor(id, req.toCommand()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<VendorResult> updateStatus(@PathVariable Long id,
                                                     @RequestBody @Valid UpdateVendorStatusRequest req) {
        return ResponseEntity.ok(updateVendorStatusUseCase.updateVendorStatus(id, req.action()));
    }
}
