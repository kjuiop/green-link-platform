package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.OnurimApplicationRequest;
import com.greenlink.shop.adapter.in.web.dto.OnurimApplicationResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.onurim.usecase.OnurimApplicationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onurim-application")
public class OnurimApplicationController {

    private final OnurimApplicationUseCase useCase;

    public OnurimApplicationController(OnurimApplicationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<OnurimApplicationResponse> latest(@AuthenticationPrincipal ShopPrincipal principal) {
        return useCase.getLatest(principal.toActor())
            .map(OnurimApplicationResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<OnurimApplicationResponse> apply(@Valid @RequestBody OnurimApplicationRequest request,
                                                           @AuthenticationPrincipal ShopPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OnurimApplicationResponse.from(
            useCase.apply(principal.toActor(), request.toCommand())));
    }
}
