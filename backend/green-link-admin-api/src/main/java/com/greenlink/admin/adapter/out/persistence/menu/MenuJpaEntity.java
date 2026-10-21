package com.greenlink.admin.adapter.out.persistence.menu;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.admin.model.Role;
import com.greenlink.core.domain.menu.model.Menu;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
public class MenuJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 200)
    private String url;

    @Column(nullable = false)
    private int sortOrder;

    @Column
    private Long parentId;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MenuRoleJpaEntity> roles = new ArrayList<>();

    protected MenuJpaEntity() {}

    public static MenuJpaEntity from(Menu menu) {
        MenuJpaEntity e = new MenuJpaEntity();
        e.id = menu.getId();
        e.name = menu.getName();
        e.url = menu.getUrl();
        e.sortOrder = menu.getSortOrder();
        e.parentId = menu.getParentId();
        return e;
    }

    public Menu toDomain() {
        List<Role> roleList = roles.stream().map(MenuRoleJpaEntity::getRole).toList();
        return Menu.of(id, name, url, sortOrder, parentId, roleList, List.of());
    }

    public void updateFields(Menu menu) {
        this.name = menu.getName();
        this.url = menu.getUrl();
        this.sortOrder = menu.getSortOrder();
        this.parentId = menu.getParentId();
    }

    public Long getId() { return id; }
    public List<MenuRoleJpaEntity> getRoles() { return roles; }
}
