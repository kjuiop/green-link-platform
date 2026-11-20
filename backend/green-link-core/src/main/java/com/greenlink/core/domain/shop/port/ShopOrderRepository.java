package com.greenlink.core.domain.shop.port;

import com.greenlink.core.domain.shop.model.ShopOrder;
import com.greenlink.core.domain.shop.model.ShopOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ShopOrderRepository {

    Optional<ShopOrder> findById(Long id);

    /** 행 잠금(SELECT ... FOR UPDATE)으로 조회한다. 동시 승인 방지용. */
    Optional<ShopOrder> findByIdForUpdate(Long id);

    Optional<ShopOrder> findByShopIdAndOrderNo(Long shopId, String orderNo);

    boolean existsByOrderNo(String orderNo);

    ShopOrder save(ShopOrder order);

    Page<ShopOrder> search(Long companyId, ShopOrderStatus status, Pageable pageable);

    long countByStatus(Long companyId, ShopOrderStatus status);
}
