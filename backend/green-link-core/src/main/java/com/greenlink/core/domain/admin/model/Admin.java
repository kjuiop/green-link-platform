package com.greenlink.core.domain.admin.model;

public class Admin {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private Long companyId;
    private AdminStatus status;

    private Admin() {}

    public static Admin create(String loginId, String encodedPassword) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.status = AdminStatus.ACTIVE;
        return a;
    }

    public static Admin createVendor(String loginId, String encodedPassword,
                                      String name, String phone, Long companyId) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.name = name;
        a.phone = phone;
        a.companyId = companyId;
        a.status = AdminStatus.ACTIVE;
        return a;
    }

    public static Admin createVendorRequest(String loginId, String encodedPassword,
                                             String name, String phone, Long companyId) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        a.name = name;
        a.phone = phone;
        a.companyId = companyId;
        a.status = AdminStatus.PENDING;
        return a;
    }

    public static Admin of(Long id, String loginId, String password, String name, String phone,
                           Long companyId, AdminStatus status) {
        Admin a = new Admin();
        a.id = id;
        a.loginId = loginId;
        a.password = password;
        a.name = name;
        a.phone = phone;
        a.companyId = companyId;
        a.status = status;
        return a;
    }

    public boolean isVendor() { return companyId != null; }

    public void activate() { this.status = AdminStatus.ACTIVE; }
    public void suspend() { this.status = AdminStatus.INACTIVE; }

    public void updateProfile(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Long getCompanyId() { return companyId; }
    public AdminStatus getStatus() { return status; }
}
