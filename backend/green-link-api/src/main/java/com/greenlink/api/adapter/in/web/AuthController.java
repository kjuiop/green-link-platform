package com.greenlink.api.adapter.in.web;

import com.greenlink.api.adapter.in.web.dto.SignupRequest;
import com.greenlink.core.application.auth.usecase.SignupUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupUseCase signupUseCase;

    public AuthController(SignupUseCase signupUseCase) {
        this.signupUseCase = signupUseCase;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest req) {
        signupUseCase.signup(req.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
