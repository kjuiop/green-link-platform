package com.greenlink.core.application.shop.usecase;

import com.greenlink.core.application.shop.dto.SaveShopSettingsCommand;
import com.greenlink.core.application.shop.dto.ShopImageUploadResult;
import com.greenlink.core.application.shop.dto.ShopSettingsResult;

public interface ManageShopSettingsUseCase {

    ShopSettingsResult get(Long companyId);

    ShopSettingsResult create(Long companyId, SaveShopSettingsCommand command);

    ShopSettingsResult update(Long companyId, SaveShopSettingsCommand command);

    ShopSettingsResult setPublished(Long companyId, boolean published);

    boolean isSlugAvailable(Long companyId, String slug);

    ShopImageUploadResult uploadImage(Long companyId, byte[] content, String contentType);
}
