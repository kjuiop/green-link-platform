package com.greenlink.core.domain.menu.model;

import com.greenlink.core.domain.admin.model.Role;

import java.util.List;

public class Menu {

    private Long id;
    private String name;
    private String url;
    private int sortOrder;
    private Long parentId;
    private List<Role> roles;
    private List<Menu> children;

    private Menu() {}

    public static Menu create(String name, String url, int sortOrder, Long parentId, List<Role> roles) {
        Menu m = new Menu();
        m.name = name;
        m.url = url;
        m.sortOrder = sortOrder;
        m.parentId = parentId;
        m.roles = roles;
        m.children = List.of();
        return m;
    }

    public static Menu of(Long id, String name, String url, int sortOrder, Long parentId,
                          List<Role> roles, List<Menu> children) {
        Menu m = new Menu();
        m.id = id;
        m.name = name;
        m.url = url;
        m.sortOrder = sortOrder;
        m.parentId = parentId;
        m.roles = roles;
        m.children = children;
        return m;
    }

    public void updateInfo(String name, String url, int sortOrder, List<Role> roles) {
        this.name = name;
        this.url = url;
        this.sortOrder = sortOrder;
        this.roles = roles;
    }

    public boolean isAccessibleBy(Role role) {
        return roles.contains(role);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public int getSortOrder() { return sortOrder; }
    public Long getParentId() { return parentId; }
    public List<Role> getRoles() { return roles; }
    public List<Menu> getChildren() { return children; }
}
