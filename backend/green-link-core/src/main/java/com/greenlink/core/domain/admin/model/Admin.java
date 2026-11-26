package com.greenlink.core.domain.admin.model;

import java.time.LocalDateTime;

public class Admin {

    public static final int MAX_PASSWORD_SETUP_FAILURES = 5;

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String email;
    private Long companyId;
    private AdminStatus status;
    private boolean superAdmin;
    private boolean onurimEnabled = true;
    private String createdAt;
    private String deletedAt;
    private LocalDateTime passwordSetupExpiresAt;
    private int passwordSetupAttempts;

    private Admin() {}

    public static Admin create(String loginId, String encodedPassword) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.status = AdminStatus.ACTIVE;
        a.superAdmin = true;
        return a;
    }

    public static Admin createOrdering(String loginId, String encodedPassword, Long companyId) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.companyId = companyId;
        a.status = AdminStatus.ACTIVE;
        return a;
    }

    public static Admin createVendor(String loginId, String encodedPassword,
                                      String name, String phone, String email, Long companyId) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.name = name;
        a.phone = phone;
        a.email = email;
        a.companyId = companyId;
        a.status = AdminStatus.ACTIVE;
        return a;
    }

    public static Admin createShopFounder(String loginId, String encodedPassword,
                                          String name, String phone, String email, Long companyId) {
        return createVendor(loginId, encodedPassword, name, phone, email, companyId);
    }

    public static Admin createShopApplicant(String loginId, String encodedPassword,
                                            String name, String phone, String email) {
        Admin a = createVendor(loginId, encodedPassword, name, phone, email, null);
        a.onurimEnabled = false;
        return a;
    }

    public static Admin createShopManaged(String loginId, String unusableEncodedPassword,
                                          String name, String phone, String email, Long companyId,
                                          LocalDateTime setupExpiresAt) {
        if (companyId == null || setupExpiresAt == null) {
            throw new IllegalArgumentException("companyId and setupExpiresAt are required");
        }
        Admin a = createVendor(loginId, unusableEncodedPassword, name, phone, email, companyId);
        a.onurimEnabled = false;
        a.passwordSetupExpiresAt = setupExpiresAt;
        return a;
    }

    public static Admin createVendorRequest(String loginId, String encodedPassword,
                                             String name, String phone, String email, Long companyId) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.name = name;
        a.phone = phone;
        a.email = email;
        a.companyId = companyId;
        a.status = AdminStatus.PENDING;
        return a;
    }

    public static Admin of(Long id, String loginId, String password, String name, String phone, String email,
                           Long companyId, AdminStatus status, String createdAt, String deletedAt) {
        Admin a = new Admin();
        a.id = id;
        a.loginId = loginId;
        a.password = password;
        a.name = name;
        a.phone = phone;
        a.email = email;
        a.companyId = companyId;
        a.status = status;
        a.createdAt = createdAt;
        a.deletedAt = deletedAt;
        return a;
    }

    public void restoreAccessFlags(boolean superAdmin, boolean onurimEnabled) {
        this.superAdmin = superAdmin;
        this.onurimEnabled = onurimEnabled;
    }

    public void restorePasswordSetup(LocalDateTime expiresAt, int attempts) {
        this.passwordSetupExpiresAt = expiresAt;
        this.passwordSetupAttempts = attempts;
    }

    public boolean isPasswordSetupPending() { return passwordSetupExpiresAt != null; }

    public boolean canSetupPassword(LocalDateTime now) {
        return isPasswordSetupPending()
            && now.isBefore(passwordSetupExpiresAt)
            && passwordSetupAttempts < MAX_PASSWORD_SETUP_FAILURES;
    }

    public void completePasswordSetup(String encodedPassword) {
        if (!isPasswordSetupPending()) {
            throw new IllegalStateException("비밀번호 설정 대기 상태가 아닙니다.");
        }
        this.password = encodedPassword;
        this.passwordSetupExpiresAt = null;
        this.passwordSetupAttempts = 0;
    }

    public void recordPasswordSetupFailure() {
        this.passwordSetupAttempts++;
    }

    public void reissuePasswordSetup(LocalDateTime newExpiresAt) {
        if (!isPasswordSetupPending()) {
            throw new IllegalStateException("이미 비밀번호가 설정된 계정입니다.");
        }
        this.passwordSetupExpiresAt = newExpiresAt;
        this.passwordSetupAttempts = 0;
    }

    public boolean isVendor() { return companyId != null; }
    public boolean isDeleted() { return deletedAt != null; }

    public void activate() { this.status = AdminStatus.ACTIVE; }
    public void suspend() { this.status = AdminStatus.INACTIVE; }
    public void softDelete(String nowKst) {
        this.status = AdminStatus.INACTIVE;
        this.deletedAt = nowKst;
    }

    public void attachToCompany(Long targetCompanyId) {
        if (targetCompanyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        if (companyId != null && !companyId.equals(targetCompanyId)) {
            throw new IllegalStateException("이미 다른 회사에 소속된 계정입니다.");
        }
        this.companyId = targetCompanyId;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Long getCompanyId() { return companyId; }
    public AdminStatus getStatus() { return status; }
    public boolean isSuperAdmin() { return superAdmin; }
    public boolean isOnurimEnabled() { return onurimEnabled; }
    public String getCreatedAt() { return createdAt; }
    public String getDeletedAt() { return deletedAt; }
    public LocalDateTime getPasswordSetupExpiresAt() { return passwordSetupExpiresAt; }
    public int getPasswordSetupAttempts() { return passwordSetupAttempts; }
}
