package com.greenlink.admin.adapter.out.persistence.menu;

import com.greenlink.core.domain.admin.model.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "menu_roles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"menu_id", "role"}))
public class MenuRoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuJpaEntity menu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected MenuRoleJpaEntity() {}

    public static MenuRoleJpaEntity of(MenuJpaEntity menu, Role role) {
        MenuRoleJpaEntity e = new MenuRoleJpaEntity();
        e.menu = menu;
        e.role = role;
        return e;
    }

    public Role getRole() { return role; }
}
