package com.greenlink.core.application.shop.service;

import com.greenlink.core.application.shop.dto.SaveShopSettingsCommand;
import com.greenlink.core.application.shop.dto.ShopImageUploadResult;
import com.greenlink.core.application.shop.dto.ShopSettingsResult;
import com.greenlink.core.application.shop.usecase.ManageShopSettingsUseCase;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.shop.model.Shop;
import com.greenlink.core.domain.shop.model.ShopSlug;
import com.greenlink.core.domain.shop.port.ShopImageStorage;
import com.greenlink.core.domain.shop.port.ShopProductRepository;
import com.greenlink.core.domain.shop.port.ShopRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.shop.ShopErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ShopSettingsService implements ManageShopSettingsUseCase {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ShopRepository shopRepository;
    private final ShopProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ShopImageStorage imageStorage;

    public ShopSettingsService(ShopRepository shopRepository, ShopProductRepository productRepository,
                               CompanyRepository companyRepository, ShopImageStorage imageStorage) {
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.imageStorage = imageStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public ShopSettingsResult get(Long companyId) {
        return shopRepository.findByCompanyId(companyId)
                .map(ShopSettingsResult::from)
                .orElseGet(() -> {
                    Company company = companyRepository.findById(companyId).orElse(null);
                    return ShopSettingsResult.notCreated(
                            company != null ? company.getCompanyName() : null,
                            company != null ? company.getPhone() : null);
                });
    }

    @Override
    @Transactional
    public ShopSettingsResult create(Long companyId, SaveShopSettingsCommand cmd) {
        if (shopRepository.findByCompanyId(companyId).isPresent()) {
            throw new BusinessException(ShopErrorCode.SHOP_ALREADY_EXISTS);
        }
        String slug = ShopSlug.normalize(cmd.slug());
        if (shopRepository.existsBySlug(slug)) {
            throw new BusinessException(ShopErrorCode.SLUG_TAKEN);
        }
        Shop shop = Shop.create(companyId, slug, cmd.displayName());
        apply(shop, cmd);
        return ShopSettingsResult.from(saveChecked(shop));
    }

    @Override
    @Transactional
    public ShopSettingsResult update(Long companyId, SaveShopSettingsCommand cmd) {
        Shop shop = shopRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.SHOP_NOT_FOUND));
        if (cmd.slug() != null && !cmd.slug().isBlank()) {
            String slug = ShopSlug.normalize(cmd.slug());
            if (!slug.equals(shop.getSlug())) {
                if (shopRepository.existsBySlug(slug)) {
                    throw new BusinessException(ShopErrorCode.SLUG_TAKEN);
                }
                shop.changeSlug(slug);
            }
        }
        apply(shop, cmd);
        return ShopSettingsResult.from(saveChecked(shop));
    }

    @Override
    @Transactional
    public ShopSettingsResult setPublished(Long companyId, boolean published) {
        Shop shop = shopRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BusinessException(ShopErrorCode.SHOP_NOT_FOUND));
        if (published && !shop.isPublishable(productRepository.countVisibleByShopId(shop.getId()))) {
            throw new BusinessException(ShopErrorCode.NOT_PUBLISHABLE);
        }
        shop.setPublished(published);
        return ShopSettingsResult.from(shopRepository.save(shop));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlugAvailable(Long companyId, String slug) {
        String normalized = ShopSlug.normalizeOrNull(slug);
        if (normalized == null) return false;
        boolean mine = shopRepository.findByCompanyId(companyId)
                .map(s -> s.getSlug().equals(normalized)).orElse(false);
        return mine || !shopRepository.existsBySlug(normalized);
    }

    @Override
    public ShopImageUploadResult uploadImage(Long companyId, byte[] content, String contentType) {
        String type = contentType == null ? "" : contentType.toLowerCase();
        if (content == null || content.length == 0 || content.length > MAX_IMAGE_BYTES
                || !IMAGE_TYPES.contains(type) || !matchesMagic(content, type)) {
            throw new BusinessException(ShopErrorCode.INVALID_IMAGE);
        }
        return new ShopImageUploadResult(imageStorage.store(companyId, content, type));
    }

    private void apply(Shop shop, SaveShopSettingsCommand c) {
        shop.updateSettings(c.displayName(), c.logoUrl(), c.phone(), c.themeColor(), c.bannerUrl(),
                c.bannerTitle(), c.bannerSubtitle(), c.notice(), c.deliveryGuide(), c.businessInfo());
    }

    private Shop saveChecked(Shop shop) {
        try {
            return shopRepository.save(shop);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ShopErrorCode.SLUG_TAKEN);
        }
    }

    private static boolean matchesMagic(byte[] b, String type) {
        return switch (type) {
            case "image/jpeg" -> b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8;
            case "image/png" -> b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/webp" -> b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }
}
