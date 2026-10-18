package com.greenlink.core.domain.user.entity;

import com.greenlink.core.domain.company.entity.Company;
import com.greenlink.core.global.jpa.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.PENDING;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> roles = new ArrayList<>();

    protected User() {}

    public static User create(String loginId, String encodedPassword,
                               String name, String phone, Company company) {
        User u = new User();
        u.loginId = loginId;
        u.password = encodedPassword;
        u.name = name;
        u.phone = phone;
        u.company = company;
        return u;
    }

    public void addRole(Role role) {
        roles.add(UserRole.of(this, role));
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Company getCompany() { return company; }
    public UserStatus getStatus() { return status; }
    public List<UserRole> getRoles() { return roles; }
}
