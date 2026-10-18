package com.greenlink.core.domain.admin.model;

public class Admin {

    private Long id;
    private String loginId;
    private String password;

    private Admin() {}

    public static Admin create(String loginId, String encodedPassword) {
        Admin a = new Admin();
        a.loginId = loginId;
        a.password = encodedPassword;
        return a;
    }

    public static Admin of(Long id, String loginId, String password) {
        Admin a = new Admin();
        a.id = id;
        a.loginId = loginId;
        a.password = password;
        return a;
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
}
