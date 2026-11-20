package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.shop.dto.SaveShopProductCommand;
import com.greenlink.core.application.shop.dto.ShopProductResult;
import com.greenlink.core.application.shop.usecase.ManageShopProductUseCase;
import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopProduct;
import com.greenlink.core.domain.shop.port.ShopProductRepository;
import com.greenlink.core.domain.shop.port.ShopRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShopProductService implements ManageShopProductUseCase {

    private final ShopRepository shopRepository;
    private final ShopProductRepository productRepository;

    public ShopProductService(ShopRepository shopRepository, ShopProductRepository productRepository) {
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopProductResult> list(Long companyId) {
        return shopRepository.findByCompanyId(companyId)
                .map(shop -> productRepository.findAllByShopId(shop.getId()).stream()
                        .map(ShopProductResult::from).toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public ShopProductResult create(Long companyId, SaveShopProductCommand c) {
        Shop shop = requireShop(companyId);
        ShopProduct product = ShopProduct.create(shop.getId(), companyId, c.category(), c.name(), c.description(),
                c.price(), c.imageUrl(), c.visible(), productRepository.maxSortOrder(shop.getId()) + 1);
        return ShopProductResult.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public ShopProductResult update(Long companyId, Long productId, SaveShopProductCommand c) {
        ShopProduct product = requireProduct(companyId, productId);
        product.update(c.category(), c.name(), c.description(), c.price(), c.imageUrl(), c.visible());
        return ShopProductResult.from(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long productId) {
        productRepository.delete(requireProduct(companyId, productId));
    }

    @Override
    @Transactional
    public void reorder(Long companyId, List<Long> orderedIds) {
        Shop shop = requireShop(companyId);
        Map<Long, ShopProduct> byId = productRepository.findAllByShopId(shop.getId()).stream()
                .collect(Collectors.toMap(ShopProduct::getId, Function.identity()));
        Set<Long> seen = new HashSet<>();
        int order = 0;
        for (Long id : orderedIds) {
            ShopProduct p = byId.get(id);
            if (p == null || !seen.add(id)) {
                throw new BusinessException(ShopErrorCode.PRODUCT_NOT_FOUND);
            }
            p.changeSortOrder(order++);
            productRepository.save(p);
        }
    }

    private Shop requireShop(Long companyId) {
        return shopRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.SHOP_NOT_FOUND));
    }

    private ShopProduct requireProduct(Long companyId, Long productId) {
        Shop shop = requireShop(companyId);
        return productRepository.findByIdAndShopId(productId, shop.getId())
                .orElseThrow(() -> new BusinessException(ShopErrorCode.PRODUCT_NOT_FOUND));
    }
}
