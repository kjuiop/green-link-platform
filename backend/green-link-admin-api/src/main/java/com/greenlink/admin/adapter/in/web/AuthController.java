package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.AdminLoginRequest;
import com.greenlink.admin.adapter.in.web.dto.VendorLoginRequest;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.dto.VendorLoginResult;
import com.greenlink.core.application.auth.usecase.AdminLoginUseCase;
import com.greenlink.core.application.auth.usecase.VendorLoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminLoginUseCase adminLoginUseCase;
    private final VendorLoginUseCase vendorLoginUseCase;

    public AuthController(AdminLoginUseCase adminLoginUseCase,
                          VendorLoginUseCase vendorLoginUseCase) {
        this.adminLoginUseCase = adminLoginUseCase;
        this.vendorLoginUseCase = vendorLoginUseCase;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResult> adminLogin(@RequestBody @Valid AdminLoginRequest req) {
        return ResponseEntity.ok(adminLoginUseCase.login(req.toCommand()));
    }

    @PostMapping("/vendor/login")
    public ResponseEntity<VendorLoginResult> vendorLogin(@RequestBody @Valid VendorLoginRequest req) {
        return ResponseEntity.ok(vendorLoginUseCase.login(req.toCommand()));
    }
}
