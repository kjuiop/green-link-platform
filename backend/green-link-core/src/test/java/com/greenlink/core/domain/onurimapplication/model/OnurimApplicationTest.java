package com.greenlink.core.domain.onurimapplication.model;

import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.ErrorCode;
import com.greenlink.core.global.exception.onurimapplication.OnurimApplicationErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OnurimApplicationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 26, 10, 0);

    private static OnurimApplicationInfo info() {
        return new OnurimApplicationInfo(" 02-123-4567 ", " 서울시 강남구 ", "  ", "tax@test.com", null,
            "02-111-2222", "국민", "123-456", "홍길동",
            List.of(new OnurimApplicationRegion(" 서울 ", " 강남구 "), new OnurimApplicationRegion("서울", "강남구"),
                new OnurimApplicationRegion(" ", "x")));
    }

    private static OnurimApplication pending() {
        return OnurimApplication.request(10L, 5L, CompanyType.BOTH, info(), null, null);
    }

    private static ErrorCode codeOf(Runnable r) {
        try {
            r.run();
        } catch (BusinessException e) {
            return e.getErrorCode();
        }
        return null;
    }

    @Test
    void 신청은_PENDING으로_생성되고_입력값이_정규화된다() {
        OnurimApplication a = pending();

        assertThat(a.getStatus()).isEqualTo(OnurimApplicationStatus.PENDING);
        assertThat(a.getInfo().companyPhone()).isEqualTo("02-123-4567");
        assertThat(a.getInfo().address()).isEqualTo("서울시 강남구");
        assertThat(a.getInfo().detailAddress()).isNull();
        assertThat(a.getInfo().faxNumber()).isEqualTo("021112222");
        assertThat(a.getInfo().regions()).containsExactly(new OnurimApplicationRegion("서울", "강남구"));
    }

    @Test
    void 희망유형_연락처_주소가_없으면_신청_거부() {
        assertThat(codeOf(() -> OnurimApplication.request(1L, 1L, null, info(), null, null)))
            .isEqualTo(OnurimApplicationErrorCode.INVALID_APPLICATION);
        OnurimApplicationInfo noPhone = new OnurimApplicationInfo(" ", "주소", null, null, null, null, null, null, null, null);
        assertThat(codeOf(() -> OnurimApplication.request(1L, 1L, CompanyType.VENDOR, noPhone, null, null)))
            .isEqualTo(OnurimApplicationErrorCode.INVALID_APPLICATION);
        OnurimApplicationInfo noAddress = new OnurimApplicationInfo("02", null, null, null, null, null, null, null, null, null);
        assertThat(codeOf(() -> OnurimApplication.request(1L, 1L, CompanyType.VENDOR, noAddress, null, null)))
            .isEqualTo(OnurimApplicationErrorCode.INVALID_APPLICATION);
    }

    @Test
    void 승인하면_APPROVED이고_승인유형과_처리자가_기록된다() {
        OnurimApplication a = pending();
        a.approve(CompanyType.ORDERING, 99L, NOW);

        assertThat(a.getStatus()).isEqualTo(OnurimApplicationStatus.APPROVED);
        assertThat(a.getApprovedType()).isEqualTo(CompanyType.ORDERING);
        assertThat(a.getProcessedBy()).isEqualTo(99L);
        assertThat(a.getProcessedAt()).isEqualTo(NOW);
        assertThat(a.isPending()).isFalse();
    }

    @Test
    void 승인유형이_없으면_승인_거부되고_상태는_유지된다() {
        OnurimApplication a = pending();

        assertThat(codeOf(() -> a.approve(null, 99L, NOW))).isEqualTo(OnurimApplicationErrorCode.TYPE_REQUIRED);
        assertThat(a.isPending()).isTrue();
    }

    @Test
    void 거절하면_REJECTED이고_사유가_기록된다() {
        OnurimApplication a = pending();
        a.reject("  서류 미비  ", 99L, NOW);

        assertThat(a.getStatus()).isEqualTo(OnurimApplicationStatus.REJECTED);
        assertThat(a.getRejectReason()).isEqualTo("서류 미비");
        assertThat(a.getApprovedType()).isNull();
    }

    @Test
    void 거절사유가_비었거나_500자_초과면_거부() {
        OnurimApplication a = pending();

        assertThat(codeOf(() -> a.reject(" ", 1L, NOW))).isEqualTo(OnurimApplicationErrorCode.REJECT_REASON_REQUIRED);
        assertThat(codeOf(() -> a.reject("가".repeat(501), 1L, NOW))).isEqualTo(OnurimApplicationErrorCode.REJECT_REASON_REQUIRED);
        assertThat(a.isPending()).isTrue();
    }

    @Test
    void 이미_처리된_신청은_승인_거절_모두_거부() {
        OnurimApplication approved = pending();
        approved.approve(CompanyType.VENDOR, 1L, NOW);
        OnurimApplication rejected = pending();
        rejected.reject("사유", 1L, NOW);

        assertThat(codeOf(() -> approved.approve(CompanyType.BOTH, 1L, NOW))).isEqualTo(OnurimApplicationErrorCode.NOT_PENDING);
        assertThat(codeOf(() -> approved.reject("사유", 1L, NOW))).isEqualTo(OnurimApplicationErrorCode.NOT_PENDING);
        assertThat(codeOf(() -> rejected.approve(CompanyType.BOTH, 1L, NOW))).isEqualTo(OnurimApplicationErrorCode.NOT_PENDING);
        assertThatThrownBy(() -> rejected.reject("재거절", 1L, NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 신청에_서류_URL을_보관한다() {
        OnurimApplication a = OnurimApplication.request(10L, 5L, CompanyType.BOTH, info(), "biz-url", "bank-url");

        assertThat(a.getBusinessRegUrl()).isEqualTo("biz-url");
        assertThat(a.getBankBookUrl()).isEqualTo("bank-url");
    }
}
