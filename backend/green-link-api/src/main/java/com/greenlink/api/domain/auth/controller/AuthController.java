package com.greenlink.api.domain.auth.controller;

import com.greenlink.api.domain.auth.dto.SignupRequest;
import com.greenlink.api.domain.auth.service.SignupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupService signupService;

    public AuthController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest req) {
        signupService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
