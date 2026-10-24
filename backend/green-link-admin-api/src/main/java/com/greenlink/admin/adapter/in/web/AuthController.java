package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.*;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.dto.FindIdResult;
import com.greenlink.core.application.auth.dto.VendorLoginResult;
import com.greenlink.core.application.auth.usecase.*;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.usecase.VendorSignupUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminLoginUseCase adminLoginUseCase;
    private final VendorLoginUseCase vendorLoginUseCase;
    private final VendorSignupUseCase vendorSignupUseCase;
    private final SendPhoneVerificationUseCase sendPhoneVerificationUseCase;
    private final VerifyPhoneCodeUseCase verifyPhoneCodeUseCase;
    private final FindIdByPhoneUseCase findIdByPhoneUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthController(AdminLoginUseCase adminLoginUseCase,
                          VendorLoginUseCase vendorLoginUseCase,
                          VendorSignupUseCase vendorSignupUseCase,
                          SendPhoneVerificationUseCase sendPhoneVerificationUseCase,
                          VerifyPhoneCodeUseCase verifyPhoneCodeUseCase,
                          FindIdByPhoneUseCase findIdByPhoneUseCase,
                          ResetPasswordUseCase resetPasswordUseCase) {
        this.adminLoginUseCase = adminLoginUseCase;
        this.vendorLoginUseCase = vendorLoginUseCase;
        this.vendorSignupUseCase = vendorSignupUseCase;
        this.sendPhoneVerificationUseCase = sendPhoneVerificationUseCase;
        this.verifyPhoneCodeUseCase = verifyPhoneCodeUseCase;
        this.findIdByPhoneUseCase = findIdByPhoneUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResult> adminLogin(@RequestBody @Valid AdminLoginRequest req) {
        return ResponseEntity.ok(adminLoginUseCase.login(req.toCommand()));
    }

    @PostMapping("/vendor/login")
    public ResponseEntity<VendorLoginResult> vendorLogin(@RequestBody @Valid VendorLoginRequest req) {
        return ResponseEntity.ok(vendorLoginUseCase.login(req.toCommand()));
    }

    @PostMapping("/signup")
    public ResponseEntity<VendorResult> signup(@RequestBody @Valid VendorSignupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorSignupUseCase.signup(req.toCommand()));
    }

    @PostMapping("/phone/send")
    public ResponseEntity<Void> sendVerification(@RequestBody @Valid SendVerificationRequest req) {
        sendPhoneVerificationUseCase.send(req.phone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<Void> verifyCode(@RequestBody @Valid VerifyCodeRequest req) {
        verifyPhoneCodeUseCase.verify(req.phone(), req.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/find-id")
    public ResponseEntity<FindIdResult> findId(@RequestBody @Valid FindIdRequest req) {
        return ResponseEntity.ok(findIdByPhoneUseCase.findId(req.phone()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        resetPasswordUseCase.reset(req.phone(), req.loginId(), req.newPassword());
        return ResponseEntity.ok().build();
    }
}
