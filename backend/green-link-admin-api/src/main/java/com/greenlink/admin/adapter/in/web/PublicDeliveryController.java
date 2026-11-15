package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.order.dto.DeliveryShareResult;
import com.greenlink.core.application.order.usecase.GetDeliveryShareUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/delivery")
public class PublicDeliveryController {

    private final GetDeliveryShareUseCase getDeliveryShareUseCase;

    public PublicDeliveryController(GetDeliveryShareUseCase getDeliveryShareUseCase) {
        this.getDeliveryShareUseCase = getDeliveryShareUseCase;
    }

    @GetMapping("/{token}")
    public ResponseEntity<DeliveryShareResult> getDeliveryShare(@PathVariable String token) {
        return getDeliveryShareUseCase.getDeliveryShare(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
