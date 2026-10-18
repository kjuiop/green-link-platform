package com.greenlink.core.domain.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"}))
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected UserRole() {}

    public static UserRole of(User user, Role role) {
        UserRole ur = new UserRole();
        ur.user = user;
        ur.role = role;
        return ur;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Role getRole() { return role; }
}
