package com.greenlink.core.domain.menu.model;

import com.greenlink.core.domain.admin.model.AdminRole;
import com.greenlink.core.domain.company.model.CompanyType;

import java.util.List;
import java.util.Set;

public class Menu {

    private Long id;
    private String name;
    private String url;
    private int sortOrder;
    private Long parentId;
    private List<AdminRole> roles;
    private List<CompanyType> companyTypes;
    private List<Menu> children;

    private Menu() {}

    public static Menu create(String name, String url, int sortOrder, Long parentId,
                              List<AdminRole> roles, List<CompanyType> companyTypes) {
        Menu m = new Menu();
        m.name = name;
        m.url = url;
        m.sortOrder = sortOrder;
        m.parentId = parentId;
        m.roles = roles;
        m.companyTypes = companyTypes;
        m.children = List.of();
        return m;
    }

    public static Menu of(Long id, String name, String url, int sortOrder, Long parentId,
                          List<AdminRole> roles, List<CompanyType> companyTypes, List<Menu> children) {
        Menu m = new Menu();
        m.id = id;
        m.name = name;
        m.url = url;
        m.sortOrder = sortOrder;
        m.parentId = parentId;
        m.roles = roles;
        m.companyTypes = companyTypes;
        m.children = children;
        return m;
    }

    public void updateInfo(String name, String url, int sortOrder,
                           List<AdminRole> roles, List<CompanyType> companyTypes) {
        this.name = name;
        this.url = url;
        this.sortOrder = sortOrder;
        this.roles = roles;
        this.companyTypes = companyTypes;
    }

    /** 관리자 권한 AND 회사 유형. 슈퍼관리자는 회사 유형 제한을 받지 않고, 회사 유형이 비어 있으면 모든 유형에 노출된다. */
    public boolean isAccessibleBy(AdminRole role, Set<CompanyType> capabilities) {
        if (role == null || !roles.contains(role)) {
            return false;
        }
        if (role == AdminRole.SUPER_ADMIN || companyTypes.isEmpty()) {
            return true;
        }
        return companyTypes.stream().anyMatch(type ->
            type == CompanyType.BOTH ? !capabilities.isEmpty() : capabilities.contains(type));
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public int getSortOrder() { return sortOrder; }
    public Long getParentId() { return parentId; }
    public List<AdminRole> getRoles() { return roles; }
    public List<CompanyType> getCompanyTypes() { return companyTypes; }
    public List<Menu> getChildren() { return children; }
}
