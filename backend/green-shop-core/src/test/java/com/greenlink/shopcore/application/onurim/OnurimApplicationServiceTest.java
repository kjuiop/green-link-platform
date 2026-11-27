package com.greenlink.shopcore.application.onurim;

import com.greenlink.shopcore.application.common.ManagementActor;
import com.greenlink.shopcore.application.common.ManagementGuard;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationStatus;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplicationView;
import com.greenlink.shopcore.application.onurim.dto.OnurimApplyCommand;
import com.greenlink.shopcore.application.onurim.dto.OnurimCompanyType;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocument;
import com.greenlink.shopcore.application.onurim.dto.OnurimDocumentRequirement;
import com.greenlink.shopcore.application.onurim.port.OnurimApplicationGateway;
import com.greenlink.shopcore.application.onurim.service.OnurimApplicationService;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OnurimApplicationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 26, 10, 0);
    private static final ManagementActor PARTNER = new ManagementActor(50L, 7L, ShopRole.PARTNER_ADMIN);

    @Mock OnurimApplicationGateway gateway;
    @Mock ShopAdminMemberRepository memberRepository;

    private OnurimApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OnurimApplicationService(gateway, new ManagementGuard(memberRepository));
    }

    private static OnurimApplyCommand command() {
        return new OnurimApplyCommand(OnurimCompanyType.BOTH, "02-111-2222", "서울시 강남구", null, null, null,
            null, null, null, null, List.of(), null, null);
    }

    private static OnurimApplicationView view(OnurimApplicationStatus status) {
        return new OnurimApplicationView(3L, status, OnurimCompanyType.BOTH, null, null, null, NOW);
    }

    private void approvedPartner(Long adminId, Long companyId) {
        given(memberRepository.findByAdminId(adminId)).willReturn(Optional.of(ShopAdminMember.of(
            1L, adminId, companyId, ShopRole.PARTNER_ADMIN, ShopRole.PARTNER_ADMIN, ShopAdminStatus.APPROVED,
            NOW.minusDays(1), NOW.minusDays(1), null, null)));
    }

    @Test
    void 승인된_파트너_관리자의_신청은_DB_기준_회사로_위임된다() {
        approvedPartner(50L, 7L);
        OnurimApplyCommand cmd = command();
        given(gateway.apply(50L, 7L, cmd)).willReturn(view(OnurimApplicationStatus.PENDING));

        OnurimApplicationView result = service.apply(new ManagementActor(50L, 999L, ShopRole.PARTNER_ADMIN), cmd);

        assertThat(result.status()).isEqualTo(OnurimApplicationStatus.PENDING);
        verify(gateway).apply(50L, 7L, cmd);
    }

    @Test
    void 브랜드_관리자는_신청할_수_없다() {
        assertThatThrownBy(() -> service.apply(new ManagementActor(60L, 7L, ShopRole.BRAND_ADMIN), command()))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
        verifyNoInteractions(gateway);
    }

    @Test
    void 슈퍼관리자는_회사가_없어_신청할_수_없다() {
        assertThatThrownBy(() -> service.apply(new ManagementActor(99L, null, ShopRole.SUPER_ADMIN), command()))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
        verifyNoInteractions(gateway);
    }

    @Test
    void 승인되지_않았거나_강등된_파트너_토큰은_거부한다() {
        given(memberRepository.findByAdminId(50L)).willReturn(Optional.of(ShopAdminMember.of(
            1L, 50L, 7L, ShopRole.BRAND_ADMIN, ShopRole.PARTNER_ADMIN, ShopAdminStatus.APPROVED,
            NOW.minusDays(1), NOW.minusDays(1), null, null)));

        assertThatThrownBy(() -> service.apply(PARTNER, command()))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
        verifyNoInteractions(gateway);
    }

    @Test
    void 상태_조회는_회사의_최신_신청을_돌려준다() {
        approvedPartner(50L, 7L);
        given(gateway.findLatest(7L)).willReturn(Optional.of(view(OnurimApplicationStatus.REJECTED)));

        assertThat(service.getLatest(PARTNER)).get()
            .extracting(OnurimApplicationView::status).isEqualTo(OnurimApplicationStatus.REJECTED);
    }

    @Test
    void 상태_조회도_파트너_관리자만_가능하다() {
        assertThatThrownBy(() -> service.getLatest(new ManagementActor(60L, 7L, ShopRole.BRAND_ADMIN)))
            .isInstanceOf(ShopBusinessException.class);
        verifyNoInteractions(gateway);
    }

    @Test
    void 신청_이력이_없으면_빈_값() {
        approvedPartner(50L, 7L);
        given(gateway.findLatest(7L)).willReturn(Optional.empty());

        assertThat(service.getLatest(PARTNER)).isEmpty();
    }

    @Test
    void 서류가_담긴_신청도_그대로_게이트웨이로_위임된다() {
        approvedPartner(50L, 7L);
        OnurimDocument biz = new OnurimDocument("biz.png", "image/png", new byte[]{1});
        OnurimDocument bank = new OnurimDocument("bank.pdf", "application/pdf", new byte[]{2});
        OnurimApplyCommand cmd = new OnurimApplyCommand(OnurimCompanyType.BOTH, "02-111-2222", "서울시 강남구", null,
            null, null, null, null, null, null, List.of(), biz, bank);
        given(gateway.apply(50L, 7L, cmd)).willReturn(view(OnurimApplicationStatus.PENDING));

        service.apply(PARTNER, cmd);

        verify(gateway).apply(50L, 7L, cmd);
    }

    @Test
    void 서류_필요_여부는_DB_기준_회사로_조회한다() {
        approvedPartner(50L, 7L);
        given(gateway.getDocumentRequirement(50L, 7L)).willReturn(new OnurimDocumentRequirement(true, false));

        OnurimDocumentRequirement result = service.getDocumentRequirement(new ManagementActor(50L, 999L, ShopRole.PARTNER_ADMIN));

        assertThat(result.needsBusinessReg()).isTrue();
        assertThat(result.needsBankBook()).isFalse();
    }

    @Test
    void 서류_필요_여부_조회도_파트너_관리자만_가능하다() {
        assertThatThrownBy(() -> service.getDocumentRequirement(new ManagementActor(60L, 7L, ShopRole.BRAND_ADMIN)))
            .isInstanceOfSatisfying(ShopBusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(ShopErrorCode.ACCESS_MANAGE_FORBIDDEN));
        verifyNoInteractions(gateway);
    }
}
