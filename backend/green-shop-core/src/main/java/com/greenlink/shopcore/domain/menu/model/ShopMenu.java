package com.greenlink.shopcore.domain.menu.model;

import com.greenlink.shopcore.domain.auth.ShopRole;

import java.util.Set;

public class ShopMenu {

    private final Long id;
    private final String name;
    private final String url;
    private final String icon;
    private final int sortOrder;
    private final Set<ShopRole> roles;

    private ShopMenu(Long id, String name, String url, String icon, int sortOrder, Set<ShopRole> roles) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.roles = Set.copyOf(roles);
    }

    public static ShopMenu create(String name, String url, String icon, int sortOrder, Set<ShopRole> roles) {
        return new ShopMenu(null, name, url, icon, sortOrder, roles);
    }

    public static ShopMenu of(Long id, String name, String url, String icon, int sortOrder, Set<ShopRole> roles) {
        return new ShopMenu(id, name, url, icon, sortOrder, roles);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getIcon() { return icon; }
    public int getSortOrder() { return sortOrder; }
    public Set<ShopRole> getRoles() { return roles; }
}
