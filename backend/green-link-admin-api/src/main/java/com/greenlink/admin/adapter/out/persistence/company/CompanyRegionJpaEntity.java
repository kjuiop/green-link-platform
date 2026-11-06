package com.greenlink.admin.adapter.out.persistence.company;

import com.greenlink.core.domain.company.model.CompanyRegion;
import jakarta.persistence.*;

@Entity
@Table(name = "company_regions")
public class CompanyRegionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 50)
    private String sido;

    @Column(nullable = false, length = 50)
    private String sigungu;

    protected CompanyRegionJpaEntity() {}

    public static CompanyRegionJpaEntity from(CompanyRegion region) {
        CompanyRegionJpaEntity e = new CompanyRegionJpaEntity();
        e.companyId = region.companyId();
        e.sido = region.sido();
        e.sigungu = region.sigungu();
        return e;
    }

    public CompanyRegion toDomain() {
        return new CompanyRegion(id, companyId, sido, sigungu);
    }
}
