package com.greenlink.shop.adapter.in.web;

import com.greenlink.shop.adapter.in.web.dto.OnurimApplicationRequest;
import com.greenlink.shop.adapter.in.web.dto.OnurimApplicationResponse;
import com.greenlink.shop.adapter.in.web.dto.OnurimDocumentRequirementResponse;
import com.greenlink.shop.security.ShopPrincipal;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocument;
import com.greenlink.shopcore.application.onurim.usecase.OnurimApplicationUseCase;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @GetMapping("/documents")
    public OnurimDocumentRequirementResponse documentRequirement(@AuthenticationPrincipal ShopPrincipal principal) {
        return OnurimDocumentRequirementResponse.from(useCase.getDocumentRequirement(principal.toActor()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OnurimApplicationResponse> apply(
            @Valid @RequestPart("data") OnurimApplicationRequest request,
            @RequestPart(value = "businessDoc", required = false) MultipartFile businessDoc,
            @RequestPart(value = "bankDoc", required = false) MultipartFile bankDoc,
            @AuthenticationPrincipal ShopPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OnurimApplicationResponse.from(
            useCase.apply(principal.toActor(), request.toCommand(toDocument(businessDoc), toDocument(bankDoc)))));
    }

    private static OnurimDocument toDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new OnurimDocument(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (IOException e) {
            throw new ShopBusinessException(ShopErrorCode.ONURIM_DOCUMENT_INVALID);
        }
    }
}
