package com.greenlink.core.application.product.service;

import com.greenlink.core.application.product.dto.VendorSearchResult;
import com.greenlink.core.application.product.usecase.SearchVendorsByProductUseCase;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyCapability;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.product.model.ProductCategory;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository.VendorMatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class VendorSearchService implements SearchVendorsByProductUseCase {

    private static final String ALL_SIGUNGU = "전체";

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final AdminRepository adminRepository;

    public VendorSearchService(ProductRepository productRepository,
                               CompanyRepository companyRepository,
                               AdminRepository adminRepository) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public List<VendorSearchResult> search(ProductCategory category, Long baseProductId, String keyword,
                                           String sido, String sigungu) {
        List<VendorMatch> matches = productRepository.findVendorMatches(category, baseProductId, keyword);
        if (matches.isEmpty()) return List.of();

        List<Long> companyIds = matches.stream().map(VendorMatch::companyId).toList();
        Map<Long, VendorMatch> matchByCompany = matches.stream()
                .collect(Collectors.toMap(VendorMatch::companyId, Function.identity()));

        Map<Long, Long> vendorAdminByCompany = adminRepository.findAllActiveByCompanyIdIn(companyIds).stream()
                .collect(Collectors.toMap(Admin::getCompanyId, Admin::getId, Math::min));
        Map<Long, List<CompanyRegion>> regions = companyRepository.findRegionsByCompanyIdIn(companyIds);
        Map<Long, List<CompanyCapability>> capabilities = companyRepository.findCapabilitiesByCompanyIdIn(companyIds);

        return companyRepository.findAllByIdIn(companyIds).stream()
                .filter(c -> c.getType() == CompanyType.VENDOR || c.getType() == CompanyType.BOTH)
                .filter(Company::isDisplayYn)
                .filter(c -> vendorAdminByCompany.containsKey(c.getId()))
                .filter(c -> matchesRegion(regions.getOrDefault(c.getId(), List.of()), sido, sigungu))
                .map(c -> toResult(c, vendorAdminByCompany.get(c.getId()), matchByCompany.get(c.getId()),
                        regions.getOrDefault(c.getId(), List.of()),
                        capabilities.getOrDefault(c.getId(), List.of())))
                .sorted(Comparator.comparingLong(VendorSearchResult::minPrice)
                        .thenComparing(VendorSearchResult::companyName))
                .toList();
    }

    private boolean matchesRegion(List<CompanyRegion> regions, String sido, String sigungu) {
        if (sido == null || sido.isBlank()) return true;
        boolean anySigungu = sigungu == null || sigungu.isBlank();
        return regions.stream().anyMatch(r -> sido.equals(r.sido())
                && (anySigungu || ALL_SIGUNGU.equals(r.sigungu()) || sigungu.equals(r.sigungu())));
    }

    private VendorSearchResult toResult(Company c, Long vendorAdminId, VendorMatch match,
                                        List<CompanyRegion> regions, List<CompanyCapability> capabilities) {
        return new VendorSearchResult(
                vendorAdminId,
                c.getCompanyName(),
                c.getPhone(),
                c.getBusinessStatus() != null ? c.getBusinessStatus().name() : null,
                regions.stream().map(r -> new VendorSearchResult.RegionDto(r.sido(), r.sigungu())).toList(),
                capabilities.stream().map(Enum::name).toList(),
                match.productCount(),
                match.minPrice());
    }
}
