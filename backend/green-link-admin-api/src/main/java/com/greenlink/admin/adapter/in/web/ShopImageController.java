package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.shop.dto.ShopImageUploadResult;
import com.greenlink.core.application.shop.usecase.ManageShopSettingsUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/shop/images")
public class ShopImageController {

    private final ManageShopSettingsUseCase useCase;
    private final ShopAccess access;

    public ShopImageController(ManageShopSettingsUseCase useCase, ShopAccess access) {
        this.useCase = useCase;
        this.access = access;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShopImageUploadResult> upload(Authentication auth,
                                                        @RequestParam(required = false) Long companyId,
                                                        @RequestParam("file") MultipartFile file) {
        Long target = access.targetCompanyId(auth, companyId);
        try {
            return ResponseEntity.ok(useCase.uploadImage(target, file.getBytes(), file.getContentType()));
        } catch (IOException e) {
            throw new BusinessException(ShopErrorCode.INVALID_IMAGE);
        }
    }
}
