package com.greenlink.core.domain.member.model;

import java.util.ArrayList;
import java.util.List;

public class Member {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private Long companyId;
    private MemberStatus status;
    private List<Role> roles;

    private Member() {
        this.roles = new ArrayList<>();
        this.status = MemberStatus.PENDING;
    }

    public static Member create(String loginId, String encodedPassword,
                                 String name, String phone, Long companyId) {
        Member m = new Member();
        m.loginId = loginId;
        m.password = encodedPassword;
        m.name = name;
        m.phone = phone;
        m.companyId = companyId;
        return m;
    }

    public static Member of(Long id, String loginId, String password,
                             String name, String phone, Long companyId,
                             MemberStatus status, List<Role> roles) {
        Member m = new Member();
        m.id = id;
        m.loginId = loginId;
        m.password = password;
        m.name = name;
        m.phone = phone;
        m.companyId = companyId;
        m.status = status;
        m.roles = new ArrayList<>(roles);
        return m;
    }

    public void addRole(Role role) { roles.add(role); }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Long getCompanyId() { return companyId; }
    public MemberStatus getStatus() { return status; }
    public List<Role> getRoles() { return roles; }
}
