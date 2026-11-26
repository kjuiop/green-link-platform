package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.*;
import com.greenlink.core.application.auth.dto.AdminLoginResult;
import com.greenlink.core.application.auth.dto.FindIdResult;
import com.greenlink.core.application.auth.dto.VendorLoginResult;
import com.greenlink.core.application.auth.usecase.*;
import com.greenlink.core.application.vendor.dto.SignupDocument;
import com.greenlink.core.application.vendor.dto.VendorSignupWithDocumentsResult;
import com.greenlink.core.application.vendor.usecase.VendorSignupWithDocumentsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminLoginUseCase adminLoginUseCase;
    private final VendorLoginUseCase vendorLoginUseCase;
    private final VendorSignupWithDocumentsUseCase vendorSignupUseCase;
    private final SendPhoneVerificationUseCase sendPhoneVerificationUseCase;
    private final VerifyPhoneCodeUseCase verifyPhoneCodeUseCase;
    private final FindIdByPhoneUseCase findIdByPhoneUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthController(AdminLoginUseCase adminLoginUseCase,
                          VendorLoginUseCase vendorLoginUseCase,
                          VendorSignupWithDocumentsUseCase vendorSignupUseCase,
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

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VendorSignupWithDocumentsResult> signup(
            @RequestPart("data") @Valid VendorSignupRequest data,
            @RequestPart("businessDoc") MultipartFile businessDoc,
            @RequestPart("bankDoc") MultipartFile bankDoc) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                vendorSignupUseCase.signup(data.toCommand(), toDocument(businessDoc), toDocument(bankDoc)));
    }

    private SignupDocument toDocument(MultipartFile file) throws IOException {
        return new SignupDocument(file.getOriginalFilename(), file.getBytes(), file.getContentType());
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
