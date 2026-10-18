package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.AdminLoginRequest;
import com.greenlink.admin.adapter.in.web.dto.SignupRequest;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.usecase.AdminLoginUseCase;
import com.greenlink.core.application.auth.usecase.SignupUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final AdminLoginUseCase adminLoginUseCase;

    public AuthController(SignupUseCase signupUseCase,
                           AdminLoginUseCase adminLoginUseCase) {
        this.signupUseCase = signupUseCase;
        this.adminLoginUseCase = adminLoginUseCase;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest req) {
        signupUseCase.signup(req.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResult> adminLogin(@RequestBody @Valid AdminLoginRequest req) {
        return ResponseEntity.ok(adminLoginUseCase.login(req.toCommand()));
    }
}
