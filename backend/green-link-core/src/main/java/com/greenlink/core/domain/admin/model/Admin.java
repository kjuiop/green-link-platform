package com.greenlink.core.domain.admin.model;

public class Admin {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String phone;

    private Admin() {}

    public static Admin create(String loginId, String encodedPassword) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        return a;
    }

    public static Admin of(Long id, String loginId, String password, String name, String phone) {
        Admin a = new Admin();
        a.id = id;
        a.loginId = loginId;
        a.password = password;
        a.name = name;
        a.phone = phone;
        return a;
    }

    public void updateProfile(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
