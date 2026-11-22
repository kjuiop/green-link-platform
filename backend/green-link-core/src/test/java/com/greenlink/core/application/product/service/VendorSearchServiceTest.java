package com.greenlink.core.application.product.service;

import com.greenlink.core.application.product.dto.VendorSearchResult;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.BusinessStatus;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyCapability;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.product.model.ProductCategory;
import com.greenlink.core.domain.product.port.ProductRepository;
import com.greenlink.core.domain.product.port.ProductRepository.VendorMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VendorSearchServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CompanyRepository companyRepository;
    @Mock AdminRepository adminRepository;
    @InjectMocks VendorSearchService service;

    private Company company(Long id, String name, CompanyType type, boolean displayYn) {
        return Company.of(id, "000-00-0000" + id, name, null, "02-000-000" + id, null, null, null,
                type, CompanyStatus.ACTIVE, displayYn, null, null, null, null, null, BusinessStatus.OPEN);
    }

    private Admin admin(Long id, Long companyId) {
        return Admin.of(id, "login" + id, "pw", "담당자", "010-0000-000" + id, null, companyId,
                AdminStatus.ACTIVE, null, null);
    }

    private void givenCommon(List<VendorMatch> matches, List<Company> companies, List<Admin> admins,
                             Map<Long, List<CompanyRegion>> regions) {
        given(productRepository.findVendorMatches(any(), any(), any())).willReturn(matches);
        given(adminRepository.findAllActiveByCompanyIdIn(anyCollection())).willReturn(admins);
        given(companyRepository.findRegionsByCompanyIdIn(anyCollection())).willReturn(regions);
        given(companyRepository.findCapabilitiesByCompanyIdIn(anyCollection()))
                .willReturn(Map.of(1L, List.of(CompanyCapability.MEMORIAL)));
        given(companyRepository.findAllByIdIn(anyList())).willReturn(companies);
    }

    @Test
    void 매칭된_상품이_없으면_빈_목록이다() {
        given(productRepository.findVendorMatches(any(), any(), any())).willReturn(List.of());

        assertThat(service.search(ProductCategory.MEMORIAL, null, null, null, null)).isEmpty();
    }

    @Test
    void 업체당_한_행으로_상품_수와_최저가를_내려주고_최저가_순으로_정렬한다() {
        givenCommon(
                List.of(new VendorMatch(1L, 3, 50000), new VendorMatch(2L, 1, 40000)),
                List.of(company(1L, "가업체", CompanyType.VENDOR, true), company(2L, "나업체", CompanyType.BOTH, true)),
                List.of(admin(11L, 1L), admin(12L, 2L)),
                Map.of(1L, List.of(CompanyRegion.of(1L, "서울특별시", "전체"))));

        List<VendorSearchResult> result = service.search(ProductCategory.MEMORIAL, null, null, null, null);

        assertThat(result).extracting(VendorSearchResult::companyName).containsExactly("나업체", "가업체");
        VendorSearchResult first = result.get(1);
        assertThat(first.vendorId()).isEqualTo(11L);
        assertThat(first.matchedProductCount()).isEqualTo(3);
        assertThat(first.minPrice()).isEqualTo(50000 + Company.DEFAULT_VENDOR_FEE);
        assertThat(first.capabilities()).containsExactly("MEMORIAL");
    }

    @Test
    void 발주사_전용_비노출_계정없는_업체는_제외한다() {
        givenCommon(
                List.of(new VendorMatch(1L, 1, 1000), new VendorMatch(2L, 1, 1000),
                        new VendorMatch(3L, 1, 1000), new VendorMatch(4L, 1, 1000)),
                List.of(company(1L, "정상", CompanyType.VENDOR, true),
                        company(2L, "발주사", CompanyType.ORDERING, true),
                        company(3L, "비노출", CompanyType.VENDOR, false),
                        company(4L, "계정없음", CompanyType.VENDOR, true)),
                List.of(admin(11L, 1L), admin(12L, 2L), admin(13L, 3L)),
                Map.of());

        List<VendorSearchResult> result = service.search(null, null, null, null, null);

        assertThat(result).extracting(VendorSearchResult::companyName).containsExactly("정상");
    }

    @Test
    void 시도와_시군구로_배송_가능_지역을_필터링한다() {
        givenCommon(
                List.of(new VendorMatch(1L, 1, 1000), new VendorMatch(2L, 1, 2000), new VendorMatch(3L, 1, 3000)),
                List.of(company(1L, "전체지역", CompanyType.VENDOR, true),
                        company(2L, "강남만", CompanyType.VENDOR, true),
                        company(3L, "부산", CompanyType.VENDOR, true)),
                List.of(admin(11L, 1L), admin(12L, 2L), admin(13L, 3L)),
                Map.of(1L, List.of(CompanyRegion.of(1L, "서울특별시", "전체")),
                        2L, List.of(CompanyRegion.of(2L, "서울특별시", "강남구")),
                        3L, List.of(CompanyRegion.of(3L, "부산광역시", "전체"))));

        assertThat(service.search(null, null, null, "서울특별시", "서초구"))
                .extracting(VendorSearchResult::companyName).containsExactly("전체지역");
        assertThat(service.search(null, null, null, "서울특별시", "강남구"))
                .extracting(VendorSearchResult::companyName).containsExactly("전체지역", "강남만");
        assertThat(service.search(null, null, null, "서울특별시", null))
                .extracting(VendorSearchResult::companyName).containsExactly("전체지역", "강남만");
    }
}
