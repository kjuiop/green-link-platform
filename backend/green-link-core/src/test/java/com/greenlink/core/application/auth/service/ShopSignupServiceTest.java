package com.greenlink.core.application.auth.service;

import com.greenlink.core.application.auth.dto.ShopSignupCommand;
import com.greenlink.core.application.auth.dto.ShopSignupResult;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.auth.AuthErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopSignupServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;

    private ShopSignupService service;

    @BeforeEach
    void setUp() {
        service = new ShopSignupService(adminRepository, companyRepository, passwordEncoder);
    }

    private ShopSignupCommand cmd(String businessNo, String requestedRole) {
        return new ShopSignupCommand("shop01", "Passw0rd!", "홍길동", "010-1234-5678", "a@test.com",
            businessNo, "꽃집", "김대표", requestedRole);
    }

    private void assertCode(Runnable call, AuthErrorCode expected) {
        assertThatThrownBy(call::run)
            .isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private void stubSaves() {
        given(passwordEncoder.encode("Passw0rd!")).willReturn("hashed");
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> {
            Admin a = inv.getArgument(0);
            Admin saved = Admin.of(10L, a.getLoginId(), a.getPassword(), a.getName(), a.getPhone(), a.getEmail(),
                a.getCompanyId(), a.getStatus(), null, null);
            saved.restoreAccess(a.getRole(), a.isOnurimEnabled());
            return saved;
        });
    }

    private Company existingCompany() {
        return Company.of(7L, "123-45-67890", "기존업체", null, null, null, null, null,
            CompanyType.VENDOR, com.greenlink.core.domain.company.model.CompanyStatus.ACTIVE, true,
            null, null, null, null, null, null);
    }

    @Test
    void 신규_사업자번호면_회사와_창립자_계정을_만든다() {
        stubSaves();
        given(adminRepository.existsByLoginId("shop01")).willReturn(false);
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.empty());
        given(companyRepository.save(any(Company.class))).willAnswer(inv -> {
            Company c = inv.getArgument(0);
            Company saved = Company.of(20L, c.getBusinessNo(), c.getCompanyName(), c.getCeoName(), c.getPhone(), null,
                null, null, null, c.getStatus(), true, null, null, null, null, null, null);
            saved.restoreServiceFlags(c.isOnurimEnabled(), c.isSiteEnabled());
            return saved;
        });

        ShopSignupResult result = service.signup(cmd("1234567890", null));

        assertThat(result.founder()).isTrue();
        assertThat(result.account().companyOnurimEnabled()).isFalse();
        assertThat(result.account().companySiteEnabled()).isTrue();
        assertThat(result.targetCompanyId()).isEqualTo(20L);
        assertThat(result.account().adminId()).isEqualTo(10L);
        assertThat(result.account().companyId()).isEqualTo(20L);
        assertThat(result.account().companyName()).isEqualTo("꽃집");

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertThat(companyCaptor.getValue().getBusinessNo()).isEqualTo("123-45-67890");
        assertThat(companyCaptor.getValue().isOnurimEnabled()).isFalse();
        assertThat(companyCaptor.getValue().isSiteEnabled()).isTrue();

        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        assertThat(adminCaptor.getValue().getPassword()).isEqualTo("hashed");
        assertThat(adminCaptor.getValue().getCompanyId()).isEqualTo(20L);
    }

    @Test
    void 이미_등록된_사업자번호면_소속없는_신청자_계정만_만들고_회사는_건드리지_않는다() {
        stubSaves();
        given(adminRepository.existsByLoginId("shop01")).willReturn(false);
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.of(existingCompany()));

        ShopSignupResult result = service.signup(cmd("123-45-67890", "BRAND_ADMIN"));

        assertThat(result.founder()).isFalse();
        assertThat(result.targetCompanyId()).isEqualTo(7L);
        assertThat(result.account().companyId()).isNull();
        assertThat(result.account().companyName()).isNull();
        assertThat(result.account().adminOnurimEnabled()).isFalse();
        verify(companyRepository, never()).save(any());
    }

    @Test
    void 이미_등록된_사업자번호에_희망_역할이_없으면_409() {
        given(adminRepository.existsByLoginId("shop01")).willReturn(false);
        given(passwordEncoder.encode("Passw0rd!")).willReturn("hashed");
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.of(existingCompany()));

        assertCode(() -> service.signup(cmd("123-45-67890", null)), AuthErrorCode.SHOP_BUSINESS_NO_REGISTERED);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 신청_불가능한_역할이면_400() {
        given(adminRepository.existsByLoginId("shop01")).willReturn(false);
        given(passwordEncoder.encode("Passw0rd!")).willReturn("hashed");
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.of(existingCompany()));

        assertCode(() -> service.signup(cmd("123-45-67890", "SUPER_ADMIN")), AuthErrorCode.SHOP_SIGNUP_ROLE_INVALID);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void 신규_회사_생성인데_회사명이_비어있으면_400() {
        given(adminRepository.existsByLoginId("shop01")).willReturn(false);
        given(passwordEncoder.encode("Passw0rd!")).willReturn("hashed");
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.empty());
        ShopSignupCommand noName = new ShopSignupCommand("shop01", "Passw0rd!", "홍길동", "010-1234-5678", null,
            "123-45-67890", " ", null, null);

        assertCode(() -> service.signup(noName), AuthErrorCode.SHOP_COMPANY_NAME_REQUIRED);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void 중복_로그인_아이디면_409() {
        given(adminRepository.existsByLoginId("shop01")).willReturn(true);

        assertCode(() -> service.signup(cmd("123-45-67890", null)), AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
        verify(adminRepository, never()).save(any());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void 사업자번호_형식이_틀리면_400() {
        assertCode(() -> service.signup(cmd("123-45", null)), AuthErrorCode.INVALID_BUSINESS_NO);
        assertCode(() -> service.signup(cmd(null, null)), AuthErrorCode.INVALID_BUSINESS_NO);
        verify(adminRepository, never()).existsByLoginId(any());
    }

    @Test
    void 동시_가입_경합으로_제약_위반이_나면_아이디_중복으로_변환한다() {
        given(adminRepository.existsByLoginId("shop01")).willReturn(false, true);
        given(passwordEncoder.encode("Passw0rd!")).willReturn("hashed");
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.empty());
        given(companyRepository.save(any(Company.class))).willThrow(new DataIntegrityViolationException("dup"));

        assertCode(() -> service.signup(cmd("123-45-67890", null)), AuthErrorCode.SHOP_LOGIN_ID_DUPLICATED);
    }

    @Test
    void 사업자번호_경합으로_제약_위반이_나면_이미_등록된_번호로_변환한다() {
        given(adminRepository.existsByLoginId("shop01")).willReturn(false, false);
        given(passwordEncoder.encode("Passw0rd!")).willReturn("hashed");
        given(companyRepository.findByBusinessNo("123-45-67890")).willReturn(Optional.empty());
        given(companyRepository.save(any(Company.class))).willThrow(new DataIntegrityViolationException("dup"));

        assertCode(() -> service.signup(cmd("123-45-67890", null)), AuthErrorCode.SHOP_BUSINESS_NO_REGISTERED);
    }

    @Test
    void 사업자번호_존재_여부는_정규화해서_조회한다() {
        given(companyRepository.existsByBusinessNo("123-45-67890")).willReturn(true);

        assertThat(service.existsBusinessNo("1234567890")).isTrue();
        assertThat(service.existsBusinessNo("123-45-67890")).isTrue();
    }

    @Test
    void 사업자번호_존재_여부_조회도_형식이_틀리면_400() {
        assertCode(() -> service.existsBusinessNo("abc"), AuthErrorCode.INVALID_BUSINESS_NO);
    }
}
