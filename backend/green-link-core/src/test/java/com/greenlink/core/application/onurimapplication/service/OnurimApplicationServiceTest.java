package com.greenlink.core.application.onurimapplication.service;

import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.dto.OnurimDocumentRequirement;
import com.greenlink.core.domain.admin.model.Admin;
import com.greenlink.core.domain.admin.model.AdminStatus;
import com.greenlink.core.domain.admin.port.AdminRepository;
import com.greenlink.core.domain.company.model.Company;
import com.greenlink.core.domain.company.model.CompanyRegion;
import com.greenlink.core.domain.company.model.CompanyStatus;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.company.port.CompanyRepository;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplication;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationInfo;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationRegion;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import com.greenlink.core.domain.onurimapplication.port.OnurimApplicationRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.ErrorCode;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OnurimApplicationServiceTest {

    private static final Long COMPANY_ID = 7L;
    private static final Long APPLICANT_ID = 1L;
    private static final Long OPERATOR_ID = 99L;
    private static final Long APP_ID = 50L;
    private static final String BIZ_URL = "https://s3/biz.png";
    private static final String BANK_URL = "https://s3/bank.png";

    @Mock OnurimApplicationRepository applicationRepository;
    @Mock CompanyRepository companyRepository;
    @Mock AdminRepository adminRepository;

    private OnurimApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OnurimApplicationService(applicationRepository, companyRepository, adminRepository);
    }

    private static Company shopCompany() {
        Company c = Company.of(COMPANY_ID, "123-45-67890", "꽃집", "김대표", null, null, null, null,
            null, CompanyStatus.ACTIVE, true, null, null, null, null, null, null);
        c.restoreServiceFlags(false, true);
        return c;
    }

    private static Company companyWithDocs(String biz, String bank) {
        Company c = Company.of(COMPANY_ID, "123-45-67890", "꽃집", "김대표", null, null, null, null,
            null, CompanyStatus.ACTIVE, true, biz, bank, null, null, null, null);
        c.restoreServiceFlags(false, true);
        return c;
    }

    private static Admin admin(Long id, Long companyId) {
        Admin a = Admin.of(id, "shop" + id, "pw", "홍길동", "010-1234-5678", null, companyId, AdminStatus.ACTIVE, null, null);
        a.restoreAccessFlags(false, false);
        return a;
    }

    private static OnurimApplicationInfo info() {
        return new OnurimApplicationInfo("02-123-4567", "서울시 강남구", "101호", "tax@test.com", "메모",
            "02-111-2222", "국민", "123-456", "홍길동",
            List.of(new OnurimApplicationRegion("서울", "강남구")));
    }

    private static OnurimApplication persisted(OnurimApplicationStatus status) {
        return persisted(status, BIZ_URL, BANK_URL);
    }

    private static OnurimApplication persisted(OnurimApplicationStatus status, String biz, String bank) {
        OnurimApplicationInfo normalized = OnurimApplication
            .request(COMPANY_ID, APPLICANT_ID, CompanyType.BOTH, info(), null, null).getInfo();
        return OnurimApplication.of(APP_ID, COMPANY_ID, APPLICANT_ID, CompanyType.BOTH, status, normalized,
            biz, bank, null, null, null, null, LocalDateTime.now());
    }

    private static void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run)
            .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private void stubApprovalTargets(Company company, Admin applicant) {
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(applicant));
        given(companyRepository.save(any(Company.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));
    }

    // ---- apply ----

    @Test
    void 신청하면_PENDING으로_저장된다() {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(shopCompany()));
        given(applicationRepository.existsPendingByCompanyId(COMPANY_ID)).willReturn(false);
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        OnurimApplicationResult result = service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL);

        assertThat(result.status()).isEqualTo(OnurimApplicationStatus.PENDING);
        assertThat(result.companyName()).isEqualTo("꽃집");
        assertThat(result.desiredType()).isEqualTo(CompanyType.BOTH);
    }

    @Test
    void 신청만으로는_회사와_계정의_온우림_권한이_켜지지_않는다() {
        Company company = shopCompany();
        Admin admin = admin(APPLICANT_ID, COMPANY_ID);
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.VENDOR, info(), BIZ_URL, BANK_URL);

        assertThat(company.isOnurimCompany()).isFalse();
        assertThat(admin.isOnurimEnabled()).isFalse();
        verify(companyRepository, never()).save(any(Company.class));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void 다른_회사_소속이거나_소속없는_계정은_신청_거부() {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, 8L)));
        assertCode(() -> service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL),
            OnurimApplicationErrorCode.APPLICANT_MISMATCH);

        given(adminRepository.findById(2L)).willReturn(Optional.of(admin(2L, null)));
        assertCode(() -> service.apply(2L, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL),
            OnurimApplicationErrorCode.APPLICANT_MISMATCH);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void 이미_온우림을_이용_중인_회사는_신청_거부() {
        Company enabled = shopCompany();
        enabled.enableOnurim(CompanyType.ORDERING);
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(enabled));

        assertCode(() -> service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL),
            OnurimApplicationErrorCode.ALREADY_ENABLED);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void 심사중인_신청이_있으면_중복_신청_거부() {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(shopCompany()));
        given(applicationRepository.existsPendingByCompanyId(COMPANY_ID)).willReturn(true);

        assertCode(() -> service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL),
            OnurimApplicationErrorCode.ALREADY_PENDING);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void 거절된_뒤에는_다시_신청할_수_있다() {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(shopCompany()));
        given(applicationRepository.existsPendingByCompanyId(COMPANY_ID)).willReturn(false);
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        assertThat(service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.ORDERING, info(), BIZ_URL, BANK_URL).status())
            .isEqualTo(OnurimApplicationStatus.PENDING);
    }

    // ---- 서류 ----

    private void stubApplyTargets(Company company) {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
    }

    @Test
    void 신청에_첨부한_서류_URL이_저장된다() {
        stubApplyTargets(shopCompany());
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        OnurimApplicationResult result =
            service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL);

        assertThat(result.businessRegUrl()).isEqualTo(BIZ_URL);
        assertThat(result.bankBookUrl()).isEqualTo(BANK_URL);
    }

    @Test
    void 회사에_서류가_없는데_첨부하지_않으면_거부() {
        stubApplyTargets(shopCompany());

        assertCode(() -> service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, null),
            OnurimApplicationErrorCode.DOCUMENT_REQUIRED);
        assertCode(() -> service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), null, BANK_URL),
            OnurimApplicationErrorCode.DOCUMENT_REQUIRED);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void 회사에_이미_있는_서류를_첨부하면_거부() {
        stubApplyTargets(companyWithDocs("https://old/biz.png", null));

        assertCode(() -> service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), BIZ_URL, BANK_URL),
            OnurimApplicationErrorCode.DOCUMENT_NOT_ALLOWED);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void 회사에_서류가_모두_있으면_첨부없이_신청된다() {
        stubApplyTargets(companyWithDocs("https://old/biz.png", "https://old/bank.png"));
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        OnurimApplicationResult result =
            service.apply(APPLICANT_ID, COMPANY_ID, CompanyType.BOTH, info(), null, null);

        assertThat(result.status()).isEqualTo(OnurimApplicationStatus.PENDING);
        assertThat(result.businessRegUrl()).isNull();
    }

    @Test
    void 필요서류는_회사에_없는_항목만_true() {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(companyWithDocs("https://old/biz.png", " ")));

        OnurimDocumentRequirement need = service.getDocumentRequirement(APPLICANT_ID, COMPANY_ID);

        assertThat(need.needsBusinessReg()).isFalse();
        assertThat(need.needsBankBook()).isTrue();
    }

    @Test
    void 필요서류_조회는_다른_회사_계정이면_거부() {
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, 8L)));

        assertCode(() -> service.getDocumentRequirement(APPLICANT_ID, COMPANY_ID),
            OnurimApplicationErrorCode.APPLICANT_MISMATCH);
    }

    @Test
    void 사전검증은_이미_이용중이거나_심사중이면_거부하고_통과하면_저장하지_않는다() {
        Company enabled = shopCompany();
        enabled.enableOnurim(CompanyType.ORDERING);
        stubApplyTargets(enabled);
        assertCode(() -> service.ensureApplicable(APPLICANT_ID, COMPANY_ID), OnurimApplicationErrorCode.ALREADY_ENABLED);

        stubApplyTargets(shopCompany());
        given(applicationRepository.existsPendingByCompanyId(COMPANY_ID)).willReturn(true);
        assertCode(() -> service.ensureApplicable(APPLICANT_ID, COMPANY_ID), OnurimApplicationErrorCode.ALREADY_PENDING);

        given(applicationRepository.existsPendingByCompanyId(COMPANY_ID)).willReturn(false);
        service.ensureApplicable(APPLICANT_ID, COMPANY_ID);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void 승인하면_회사에_없는_서류만_신청서에서_복사된다() {
        Company company = shopCompany();
        stubApprovalTargets(company, admin(APPLICANT_ID, COMPANY_ID));

        service.approve(APP_ID, CompanyType.BOTH, OPERATOR_ID);

        assertThat(company.getBusinessRegUrl()).isEqualTo(BIZ_URL);
        assertThat(company.getBankBookUrl()).isEqualTo(BANK_URL);
    }

    @Test
    void 승인해도_회사의_기존_서류는_덮어쓰지_않는다() {
        Company company = companyWithDocs("https://old/biz.png", null);
        stubApprovalTargets(company, admin(APPLICANT_ID, COMPANY_ID));

        service.approve(APP_ID, CompanyType.BOTH, OPERATOR_ID);

        assertThat(company.getBusinessRegUrl()).isEqualTo("https://old/biz.png");
        assertThat(company.getBankBookUrl()).isEqualTo(BANK_URL);
    }

    @Test
    void 신청에_서류가_없으면_승인해도_회사_서류는_그대로다() {
        Company company = companyWithDocs("https://old/biz.png", "https://old/bank.png");
        given(applicationRepository.findById(APP_ID))
            .willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING, null, null)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));
        given(companyRepository.save(any(Company.class))).willAnswer(inv -> inv.getArgument(0));
        given(adminRepository.save(any(Admin.class))).willAnswer(inv -> inv.getArgument(0));
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        service.approve(APP_ID, CompanyType.ORDERING, OPERATOR_ID);

        assertThat(company.getBusinessRegUrl()).isEqualTo("https://old/biz.png");
        assertThat(company.getBankBookUrl()).isEqualTo("https://old/bank.png");
    }

    @Test
    void 거절하면_회사에_서류가_복사되지_않는다() {
        Company company = shopCompany();
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING)));
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        service.reject(APP_ID, "서류 불충분", OPERATOR_ID);

        assertThat(company.getBusinessRegUrl()).isNull();
        verify(companyRepository, never()).save(any(Company.class));
    }

    // ---- approve ----

    @Test
    void 승인하면_회사_유형_플래그_신청자_플래그가_켜지고_정보가_복사된다() {
        Company company = shopCompany();
        Admin applicant = admin(APPLICANT_ID, COMPANY_ID);
        stubApprovalTargets(company, applicant);

        OnurimApplicationResult result = service.approve(APP_ID, CompanyType.BOTH, OPERATOR_ID);

        assertThat(result.status()).isEqualTo(OnurimApplicationStatus.APPROVED);
        assertThat(result.approvedType()).isEqualTo(CompanyType.BOTH);
        assertThat(company.isOnurimCompany()).isTrue();
        assertThat(company.getType()).isEqualTo(CompanyType.BOTH);
        assertThat(applicant.isOnurimEnabled()).isTrue();
        assertThat(company.getPhone()).isEqualTo("02-123-4567");
        assertThat(company.getAddress()).isEqualTo("서울시 강남구");
        assertThat(company.getDetailAddress()).isEqualTo("101호");
        assertThat(company.getTaxInvoiceEmail()).isEqualTo("tax@test.com");
        assertThat(company.getFaxNumber()).isEqualTo("021112222");
        assertThat(company.isFaxEnabled()).isFalse();
        assertThat(company.getBankName()).isEqualTo("국민");
        assertThat(company.getAccountNumber()).isEqualTo("123-456");
        assertThat(company.getAccountHolder()).isEqualTo("홍길동");
        assertThat(company.getCompanyName()).isEqualTo("꽃집");
        assertThat(company.getBusinessNo()).isEqualTo("123-45-67890");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CompanyRegion>> regions = ArgumentCaptor.forClass(List.class);
        verify(companyRepository).saveRegions(anyLong(), regions.capture());
        assertThat(regions.getValue()).extracting(CompanyRegion::sido, CompanyRegion::sigungu)
            .containsExactly(org.assertj.core.groups.Tuple.tuple("서울", "강남구"));
    }

    @Test
    void 발주사로_승인하면_수주사_정보는_복사하지_않는다() {
        Company company = shopCompany();
        stubApprovalTargets(company, admin(APPLICANT_ID, COMPANY_ID));

        service.approve(APP_ID, CompanyType.ORDERING, OPERATOR_ID);

        assertThat(company.getType()).isEqualTo(CompanyType.ORDERING);
        assertThat(company.getBankName()).isNull();
        assertThat(company.getAccountNumber()).isNull();
        verify(companyRepository, never()).saveRegions(anyLong(), anyList());
    }

    @Test
    void 승인해도_같은_회사의_다른_계정은_변경되지_않는다() {
        stubApprovalTargets(shopCompany(), admin(APPLICANT_ID, COMPANY_ID));
        Admin other = admin(2L, COMPANY_ID);

        service.approve(APP_ID, CompanyType.VENDOR, OPERATOR_ID);

        assertThat(other.isOnurimEnabled()).isFalse();
        ArgumentCaptor<Admin> saved = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(APPLICANT_ID);
    }

    @Test
    void 승인유형이_없으면_아무것도_바뀌지_않는다() {
        Company company = shopCompany();
        Admin applicant = admin(APPLICANT_ID, COMPANY_ID);
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(company));
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(applicant));

        assertCode(() -> service.approve(APP_ID, null, OPERATOR_ID), OnurimApplicationErrorCode.TYPE_REQUIRED);

        assertThat(company.isOnurimCompany()).isFalse();
        assertThat(applicant.isOnurimEnabled()).isFalse();
        verify(companyRepository, never()).save(any(Company.class));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void 이미_처리된_신청은_승인_거부() {
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.REJECTED)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(shopCompany()));
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));

        assertCode(() -> service.approve(APP_ID, CompanyType.BOTH, OPERATOR_ID), OnurimApplicationErrorCode.NOT_PENDING);
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void 심사중_이미_온우림이_켜진_회사는_승인_거부() {
        Company enabled = shopCompany();
        enabled.enableOnurim(CompanyType.ORDERING);
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING)));
        given(companyRepository.findById(COMPANY_ID)).willReturn(Optional.of(enabled));
        given(adminRepository.findById(APPLICANT_ID)).willReturn(Optional.of(admin(APPLICANT_ID, COMPANY_ID)));

        assertCode(() -> service.approve(APP_ID, CompanyType.BOTH, OPERATOR_ID), OnurimApplicationErrorCode.ALREADY_ENABLED);
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void 없는_신청은_404() {
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.empty());

        assertCode(() -> service.approve(APP_ID, CompanyType.BOTH, OPERATOR_ID), OnurimApplicationErrorCode.NOT_FOUND);
    }

    // ---- reject ----

    @Test
    void 거절하면_사유가_기록되고_회사와_계정은_그대로다() {
        Company company = shopCompany();
        Admin applicant = admin(APPLICANT_ID, COMPANY_ID);
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING)));
        given(applicationRepository.save(any(OnurimApplication.class))).willAnswer(inv -> inv.getArgument(0));

        OnurimApplicationResult result = service.reject(APP_ID, "서류 미비", OPERATOR_ID);

        assertThat(result.status()).isEqualTo(OnurimApplicationStatus.REJECTED);
        assertThat(result.rejectReason()).isEqualTo("서류 미비");
        assertThat(company.isOnurimCompany()).isFalse();
        assertThat(applicant.isOnurimEnabled()).isFalse();
        verify(companyRepository, never()).save(any(Company.class));
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void 사유없는_거절과_이미_처리된_신청의_거절은_거부() {
        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.PENDING)));
        assertCode(() -> service.reject(APP_ID, " ", OPERATOR_ID), OnurimApplicationErrorCode.REJECT_REASON_REQUIRED);

        given(applicationRepository.findById(APP_ID)).willReturn(Optional.of(persisted(OnurimApplicationStatus.APPROVED)));
        assertCode(() -> service.reject(APP_ID, "사유", OPERATOR_ID), OnurimApplicationErrorCode.NOT_PENDING);
        verify(applicationRepository, never()).save(any());
    }

    // ---- query ----

    @Test
    void 최신_신청이_없으면_빈_결과() {
        given(applicationRepository.findLatestByCompanyId(COMPANY_ID)).willReturn(Optional.empty());

        assertThat(service.getLatest(COMPANY_ID)).isEmpty();
    }
}
