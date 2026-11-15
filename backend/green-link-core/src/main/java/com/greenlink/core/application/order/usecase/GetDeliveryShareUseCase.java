package com.greenlink.core.application.order.usecase;

import com.greenlink.core.application.order.dto.DeliveryShareResult;

import java.util.Optional;

public interface GetDeliveryShareUseCase {

    Optional<DeliveryShareResult> getDeliveryShare(String photoToken);
}
