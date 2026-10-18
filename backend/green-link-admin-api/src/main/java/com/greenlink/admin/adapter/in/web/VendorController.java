package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.CreateVendorRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateVendorRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateVendorStatusRequest;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.usecase.CreateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorsUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorStatusUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public VendorController(GetVendorsUseCase getVendorsUseCase,
                            GetVendorUseCase getVendorUseCase,
                            CreateVendorUseCase createVendorUseCase,
                            UpdateVendorUseCase updateVendorUseCase,
                            UpdateVendorStatusUseCase updateVendorStatusUseCase,
                            PasswordEncoder passwordEncoder) {
        this.getVendorsUseCase = getVendorsUseCase;
        this.getVendorUseCase = getVendorUseCase;
        this.createVendorUseCase = createVendorUseCase;
        this.updateVendorUseCase = updateVendorUseCase;
        this.updateVendorStatusUseCase = updateVendorStatusUseCase;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<VendorResult>> getAll(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(getVendorsUseCase.getVendors(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResult> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(getVendorUseCase.getVendor(id));
    }

    @PostMapping
    public ResponseEntity<VendorResult> create(@RequestBody @Valid CreateVendorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createVendorUseCase.createVendor(req.toCommand(passwordEncoder)));
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
