package com.greenlink.admin.adapter.out.persistence.menu;

import com.greenlink.core.domain.company.model.CompanyType;
import jakarta.persistence.*;

@Entity
@Table(name = "menu_company_types",
       uniqueConstraints = @UniqueConstraint(columnNames = {"menu_id", "company_type"}))
public class MenuCompanyTypeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuJpaEntity menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false, length = 20)
    private CompanyType companyType;

    protected MenuCompanyTypeJpaEntity() {}

    public static MenuCompanyTypeJpaEntity of(MenuJpaEntity menu, CompanyType companyType) {
        MenuCompanyTypeJpaEntity e = new MenuCompanyTypeJpaEntity();
        e.menu = menu;
        e.companyType = companyType;
        return e;
    }

    public CompanyType getCompanyType() { return companyType; }
}
