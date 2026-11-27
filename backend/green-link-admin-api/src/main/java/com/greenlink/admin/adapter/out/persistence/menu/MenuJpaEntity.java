package com.greenlink.admin.adapter.out.persistence.menu;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;
import com.greenlink.core.domain.menu.model.Menu;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private Set<MenuRoleJpaEntity> roles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MenuCompanyTypeJpaEntity> companyTypes = new LinkedHashSet<>();

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
        List<AdminRole> roleList = roles.stream().map(MenuRoleJpaEntity::getRole).toList();
        List<CompanyType> typeList = companyTypes.stream().map(MenuCompanyTypeJpaEntity::getCompanyType).toList();
        return Menu.of(id, name, url, sortOrder, parentId, roleList, typeList, List.of());
    }

    public void updateFields(Menu menu) {
        this.name = menu.getName();
        this.url = menu.getUrl();
        this.sortOrder = menu.getSortOrder();
        this.parentId = menu.getParentId();
    }

    public Long getId() { return id; }
    public Set<MenuRoleJpaEntity> getRoles() { return roles; }
    public Set<MenuCompanyTypeJpaEntity> getCompanyTypes() { return companyTypes; }
}
