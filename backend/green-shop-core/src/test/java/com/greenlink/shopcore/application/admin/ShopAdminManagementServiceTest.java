package com.greenlink.shopcore.application.admin;

import com.greenlink.shopcore.application.admin.dto.ShopAdminView;
import com.greenlink.shopcore.application.admin.service.ShopAdminManagementService;
import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.member.model.ShopAdminMember;
import com.greenlink.shopcore.domain.member.model.ShopAdminStatus;
import com.greenlink.shopcore.domain.member.port.ShopAdminMemberRepository;
import com.greenlink.shopcore.global.exception.ErrorCode;
import com.greenlink.shopcore.global.exception.ShopBusinessException;
import com.greenlink.shopcore.global.exception.ShopErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShopAdminManagementServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Mock ShopAdminMemberRepository memberRepository;

    private ShopAdminManagementService service;

    @BeforeEach
    void setUp() {
        service = new ShopAdminManagementService(memberRepository, CLOCK);
    }

    private ShopAdminMember member(ShopAdminStatus status) {
        return ShopAdminMember.of(1L, 10L, "login", "이름", "회사", status, NOW.minusDays(1), null, null, null);
    }

    private void assertCode(Runnable call, ErrorCode expected) {
        assertThatThrownBy(call::run).isInstanceOfSatisfying(ShopBusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    void 목록은_상태_필터를_저장소에_전달하고_SHOP_ADMIN_역할로_변환한다() {
        given(memberRepository.findAll(ShopAdminStatus.PENDING)).willReturn(List.of(member(ShopAdminStatus.PENDING)));

        List<ShopAdminView> result = service.list(ShopAdminStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(ShopRole.SHOP_ADMIN);
        assertThat(result.get(0).status()).isEqualTo(ShopAdminStatus.PENDING);
    }

    @Test
    void 승인하면_처리자와_시각이_기록되어_저장된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.approve(1L, 99L);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.APPROVED);
        assertThat(m.getProcessedBy()).isEqualTo(99L);
        assertThat(m.getProcessedAt()).isEqualTo(NOW);
        verify(memberRepository).save(m);
    }

    @Test
    void 거절하면_사유가_기록되어_저장된다() {
        ShopAdminMember m = member(ShopAdminStatus.PENDING);
        given(memberRepository.findById(1L)).willReturn(Optional.of(m));

        service.reject(1L, "서류 미비", 99L);

        assertThat(m.getStatus()).isEqualTo(ShopAdminStatus.REJECTED);
        assertThat(m.getRejectReason()).isEqualTo("서류 미비");
        verify(memberRepository).save(m);
    }

    @Test
    void 사유_없는_거절은_거부되고_저장되지_않는다() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member(ShopAdminStatus.PENDING)));

        assertCode(() -> service.reject(1L, " ", 99L), ShopErrorCode.ACCESS_REJECT_REASON_REQUIRED);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 이미_처리된_건은_승인_거절이_거부된다() {
        given(memberRepository.findById(1L))
            .willReturn(Optional.of(member(ShopAdminStatus.APPROVED)), Optional.of(member(ShopAdminStatus.REJECTED)));

        assertCode(() -> service.approve(1L, 99L), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        assertCode(() -> service.reject(1L, "사유", 99L), ShopErrorCode.ACCESS_INVALID_TRANSITION);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 없는_id는_ADMIN_REQUEST_NOT_FOUND() {
        given(memberRepository.findById(5L)).willReturn(Optional.empty());

        assertCode(() -> service.approve(5L, 99L), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
        assertCode(() -> service.reject(5L, "사유", 99L), ShopErrorCode.ADMIN_REQUEST_NOT_FOUND);
    }
}
