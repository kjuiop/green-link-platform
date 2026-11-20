package com.greenlink.core.domain.shop.port;

public interface ShopImageStorage {

    /** 이미지를 저장하고 공개 URL을 돌려준다. */
    String store(Long companyId, byte[] content, String contentType);
}
