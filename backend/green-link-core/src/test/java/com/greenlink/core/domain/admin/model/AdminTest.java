package com.greenlink.core.domain.admin.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTest {

    @Test
    void createVendor_setsStatusToActive() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(admin.getCompanyId()).isEqualTo(1L);
    }

    @Test
    void createShopFounder_회사에_소속된_활성_계정이다() {
        Admin admin = Admin.createShopFounder("shop01", "encodedPw", "홍길동", "010-1234-5678", "a@test.com", 9L);

        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(admin.getCompanyId()).isEqualTo(9L);
        assertThat(admin.isSuperAdmin()).isFalse();
        assertThat(admin.isOnurimEnabled()).isTrue();
    }

    @Test
    void createShopApplicant_소속없고_온우림_접근이_꺼진_활성_계정이다() {
        Admin admin = Admin.createShopApplicant("shop02", "encodedPw", "김철수", "010-1234-5678", null);

        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(admin.getCompanyId()).isNull();
        assertThat(admin.isOnurimEnabled()).isFalse();
        assertThat(admin.isSuperAdmin()).isFalse();
    }

    @Test
    void createVendorRequest_setsStatusToPending() {
        Admin admin = Admin.createVendorRequest("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.PENDING);
    }

    @Test
    void isVendor_whenCompanyIdIsNull_returnsFalse() {
        Admin admin = Admin.create("superadmin", "encodedPw");
        assertThat(admin.isVendor()).isFalse();
    }

    @Test
    void isVendor_whenCompanyIdIsNotNull_returnsTrue() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.isVendor()).isTrue();
    }

    @Test
    void activate_setsStatusToActive() {
        Admin admin = Admin.createVendorRequest("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.PENDING);

        admin.activate();

        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
    }

    @Test
    void suspend_setsStatusToInactive() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);

        admin.suspend();

        assertThat(admin.getStatus()).isEqualTo(AdminStatus.INACTIVE);
    }

    @Test
    void updateProfile_updatesNamePhoneAndEmail() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        admin.updateProfile("김철수", "010-9999-0000", "new@test.com");
        assertThat(admin.getName()).isEqualTo("김철수");
        assertThat(admin.getPhone()).isEqualTo("010-9999-0000");
        assertThat(admin.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void create_슈퍼관리자_플래그가_켜진다() {
        Admin admin = Admin.create("superadmin", "encodedPw");
        assertThat(admin.isSuperAdmin()).isTrue();
        assertThat(admin.isOnurimEnabled()).isTrue();
    }

    @Test
    void createVendor_슈퍼관리자가_아니고_온우림사용은_켜진다() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.isSuperAdmin()).isFalse();
        assertThat(admin.isOnurimEnabled()).isTrue();
    }

    @Test
    void create_시드_관리자는_슈퍼관리자_권한이다() {
        Admin admin = Admin.create("admin", "encodedPw");
        assertThat(admin.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(admin.isSuperAdmin()).isTrue();
    }

    @Test
    void createVendor_기본_권한은_파트너_관리자다() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        assertThat(admin.getRole()).isEqualTo(AdminRole.PARTNER_ADMIN);
    }

    @Test
    void restoreAccess_저장된_값으로_복원한다() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", "vendor01@test.com", 1L);
        admin.restoreAccess(AdminRole.PARTNER_ADMIN, false);
        assertThat(admin.isSuperAdmin()).isFalse();
        assertThat(admin.isOnurimEnabled()).isFalse();
    }

    @Test
    void attachToCompany_소속없는_계정은_회사에_연결된다() {
        Admin admin = Admin.createShopApplicant("shop02", "encodedPw", "김철수", "010-1234-5678", null);

        admin.attachToCompany(7L);

        assertThat(admin.getCompanyId()).isEqualTo(7L);
        assertThat(admin.isOnurimEnabled()).isFalse();
    }

    @Test
    void attachToCompany_같은_회사면_변경이_없다() {
        Admin admin = Admin.createShopFounder("shop01", "encodedPw", "홍길동", "010-1234-5678", null, 7L);

        admin.attachToCompany(7L);

        assertThat(admin.getCompanyId()).isEqualTo(7L);
    }

    @Test
    void attachToCompany_다른_회사에_소속된_계정은_거부한다() {
        Admin admin = Admin.createShopFounder("shop01", "encodedPw", "홍길동", "010-1234-5678", null, 7L);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> admin.attachToCompany(8L))
            .isInstanceOf(IllegalStateException.class);
        assertThat(admin.getCompanyId()).isEqualTo(7L);
    }

    @Test
    void attachToCompany_회사_id가_없으면_거부한다() {
        Admin admin = Admin.createShopApplicant("shop02", "encodedPw", "김철수", "010-1234-5678", null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> admin.attachToCompany(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
