package com.greenlink.shop.adapter.out.persistence.menu;

import com.greenlink.shopcore.domain.auth.ShopRole;
import com.greenlink.shopcore.domain.menu.model.ShopMenu;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shop_menus")
public class ShopMenuJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String url;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shop_menu_roles", joinColumns = @JoinColumn(name = "menu_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Set<ShopRole> roles = new HashSet<>();

    protected ShopMenuJpaEntity() {}

    static ShopMenuJpaEntity from(ShopMenu m) {
        ShopMenuJpaEntity e = new ShopMenuJpaEntity();
        e.id = m.getId();
        e.name = m.getName();
        e.url = m.getUrl();
        e.icon = m.getIcon();
        e.sortOrder = m.getSortOrder();
        e.roles = new HashSet<>(m.getRoles());
        return e;
    }

    ShopMenu toDomain() {
        Set<ShopRole> copy = roles.isEmpty() ? EnumSet.noneOf(ShopRole.class) : EnumSet.copyOf(roles);
        return ShopMenu.of(id, name, url, icon, sortOrder, copy);
    }
}
