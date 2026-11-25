package com.greenlink.core.domain.admin.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminPasswordSetupTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 26, 12, 0);

    private Admin pending() {
        return Admin.createShopManaged("shop01", "randomHash", "홍길동", "010-1234-5678", null, 7L, NOW.plusDays(7));
    }

    @Test
    void 생성된_계정은_온우림_비활성_설정_대기_상태다() {
        Admin a = pending();

        assertThat(a.isOnurimEnabled()).isFalse();
        assertThat(a.getStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(a.getCompanyId()).isEqualTo(7L);
        assertThat(a.isPasswordSetupPending()).isTrue();
        assertThat(a.canSetupPassword(NOW)).isTrue();
    }

    @Test
    void 회사나_만료시각이_없으면_생성할_수_없다() {
        assertThatThrownBy(() -> Admin.createShopManaged("shop01", "h", "n", "p", null, null, NOW))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Admin.createShopManaged("shop01", "h", "n", "p", null, 7L, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 만료시각이_지나면_설정할_수_없다() {
        assertThat(pending().canSetupPassword(NOW.plusDays(7))).isFalse();
        assertThat(pending().canSetupPassword(NOW.plusDays(8))).isFalse();
    }

    @Test
    void 실패가_5회_누적되면_잠긴다() {
        Admin a = pending();
        for (int i = 0; i < 4; i++) {
            a.recordPasswordSetupFailure();
        }
        assertThat(a.canSetupPassword(NOW)).isTrue();

        a.recordPasswordSetupFailure();

        assertThat(a.getPasswordSetupAttempts()).isEqualTo(5);
        assertThat(a.canSetupPassword(NOW)).isFalse();
    }

    @Test
    void 설정을_완료하면_대기가_해제되고_다시_설정할_수_없다() {
        Admin a = pending();
        a.recordPasswordSetupFailure();

        a.completePasswordSetup("newHash");

        assertThat(a.getPassword()).isEqualTo("newHash");
        assertThat(a.isPasswordSetupPending()).isFalse();
        assertThat(a.getPasswordSetupAttempts()).isZero();
        assertThat(a.canSetupPassword(NOW)).isFalse();
        assertThatThrownBy(() -> a.completePasswordSetup("again")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 재발급하면_만료가_연장되고_실패_횟수가_초기화된다() {
        Admin a = pending();
        for (int i = 0; i < 5; i++) {
            a.recordPasswordSetupFailure();
        }

        a.reissuePasswordSetup(NOW.plusDays(14));

        assertThat(a.getPasswordSetupAttempts()).isZero();
        assertThat(a.canSetupPassword(NOW.plusDays(10))).isTrue();
    }

    @Test
    void 이미_설정된_계정은_재발급할_수_없다() {
        Admin a = Admin.createVendor("v01", "hash", "업체", "010-0000-0000", null, 7L);

        assertThat(a.isPasswordSetupPending()).isFalse();
        assertThatThrownBy(() -> a.reissuePasswordSetup(NOW.plusDays(7))).isInstanceOf(IllegalStateException.class);
    }
}
