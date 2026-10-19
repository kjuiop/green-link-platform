package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.UpdateAdminProfileRequest;
import com.greenlink.core.application.admin.dto.AdminProfileResult;
import com.greenlink.core.application.admin.usecase.GetAdminProfileUseCase;
import com.greenlink.core.application.admin.usecase.UpdateAdminProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final GetAdminProfileUseCase getAdminProfileUseCase;
    private final UpdateAdminProfileUseCase updateAdminProfileUseCase;

    public AdminController(GetAdminProfileUseCase getAdminProfileUseCase,
                           UpdateAdminProfileUseCase updateAdminProfileUseCase) {
        this.getAdminProfileUseCase = getAdminProfileUseCase;
        this.updateAdminProfileUseCase = updateAdminProfileUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<AdminProfileResult> getMe(Authentication authentication) {
        Long adminId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(getAdminProfileUseCase.getProfile(adminId));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMe(Authentication authentication,
                                         @RequestBody @Valid UpdateAdminProfileRequest req) {
        Long adminId = Long.parseLong(authentication.getName());
        updateAdminProfileUseCase.updateProfile(req.toCommand(adminId));
        return ResponseEntity.ok().build();
    }
}
