package com.greenlink.admin.adapter.out.persistence.regionguide;

import com.greenlink.admin.adapter.out.persistence.BaseJpaEntity;
import com.greenlink.core.domain.regionguide.model.RegionGuide;
import com.greenlink.core.domain.regionguide.model.RegionGuideCategory;
import jakarta.persistence.*;

import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "region_guides",
        indexes = @Index(name = "idx_region_guides_sido_active", columnList = "sido, active"))
public class RegionGuideJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String sido;

    @Column(length = 30)
    private String sigungu;

    @Column(name = "place_name", length = 100)
    private String placeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegionGuideCategory category;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(length = 200)
    private String aliases;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected RegionGuideJpaEntity() {}

    public static RegionGuideJpaEntity from(RegionGuide g) {
        RegionGuideJpaEntity e = new RegionGuideJpaEntity();
        e.id = g.getId();
        e.sido = g.getSido();
        e.sigungu = g.getSigungu();
        e.placeName = g.getPlaceName();
        e.category = g.getCategory();
        e.content = g.getContent();
        e.aliases = g.getAliases();
        e.sortOrder = g.getSortOrder();
        e.active = g.isActive();
        e.updatedBy = g.getUpdatedBy();
        return e;
    }

    public RegionGuide toDomain() {
        return RegionGuide.of(id, sido, sigungu, placeName, category, content, aliases, sortOrder, active,
                updatedBy,
                getUpdatedAt() != null
                        ? getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : null);
    }
}
