package com.greenlink.core.domain.admin.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTest {

    @Test
    void createVendor_setsStatusToActive() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(admin.getCompanyId()).isEqualTo(1L);
    }

    @Test
    void createVendorRequest_setsStatusToPending() {
        Admin admin = Admin.createVendorRequest("vendor01", "encodedPw", "홍길동", "010-1234-5678", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.PENDING);
    }

    @Test
    void isVendor_whenCompanyIdIsNull_returnsFalse() {
        Admin admin = Admin.create("superadmin", "encodedPw");
        assertThat(admin.isVendor()).isFalse();
    }

    @Test
    void isVendor_whenCompanyIdIsNotNull_returnsTrue() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", 1L);
        assertThat(admin.isVendor()).isTrue();
    }

    @Test
    void activate_setsStatusToActive() {
        Admin admin = Admin.createVendorRequest("vendor01", "encodedPw", "홍길동", "010-1234-5678", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.PENDING);

        admin.activate();

        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
    }

    @Test
    void suspend_setsStatusToInactive() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", 1L);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);

        admin.suspend();

        assertThat(admin.getStatus()).isEqualTo(AdminStatus.INACTIVE);
    }

    @Test
    void updateProfile_updatesNameAndPhone() {
        Admin admin = Admin.createVendor("vendor01", "encodedPw", "홍길동", "010-1234-5678", 1L);
        admin.updateProfile("김철수", "010-9999-0000");
        assertThat(admin.getName()).isEqualTo("김철수");
        assertThat(admin.getPhone()).isEqualTo("010-9999-0000");
    }
}
