package com.greenlink.core.domain.regionguide.model;

import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.regionguide.RegionGuideErrorCode;

import java.util.Set;

public class RegionGuide {

    private static final Set<String> SIDO_NAMES = Set.of(
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시",
            "울산광역시", "세종특별자치시", "경기도", "강원특별자치도", "충청북도", "충청남도",
            "전북특별자치도", "전라남도", "경상북도", "경상남도", "제주특별자치도");

    private Long id;
    private String sido;
    private String sigungu;
    private String placeName;
    private RegionGuideCategory category;
    private String content;
    private String aliases;
    private int sortOrder;
    private boolean active;
    private Long updatedBy;
    private String updatedAt;

    private RegionGuide() {}

    public static RegionGuide create(String sido, String sigungu, String placeName,
                                     RegionGuideCategory category, String content,
                                     String aliases, int sortOrder, Long updatedBy) {
        RegionGuide g = new RegionGuide();
        g.active = true;
        g.apply(sido, sigungu, placeName, category, content, aliases, sortOrder, updatedBy);
        return g;
    }

    public static RegionGuide of(Long id, String sido, String sigungu, String placeName,
                                 RegionGuideCategory category, String content, String aliases,
                                 int sortOrder, boolean active, Long updatedBy, String updatedAt) {
        RegionGuide g = new RegionGuide();
        g.id = id;
        g.sido = sido;
        g.sigungu = sigungu;
        g.placeName = placeName;
        g.category = category;
        g.content = content;
        g.aliases = aliases;
        g.sortOrder = sortOrder;
        g.active = active;
        g.updatedBy = updatedBy;
        g.updatedAt = updatedAt;
        return g;
    }

    public void update(String sido, String sigungu, String placeName,
                       RegionGuideCategory category, String content,
                       String aliases, int sortOrder, Long updatedBy) {
        apply(sido, sigungu, placeName, category, content, aliases, sortOrder, updatedBy);
    }

    public void changeActive(boolean active, Long updatedBy) {
        this.active = active;
        this.updatedBy = updatedBy;
    }

    private void apply(String sido, String sigungu, String placeName,
                       RegionGuideCategory category, String content,
                       String aliases, int sortOrder, Long updatedBy) {
        if (sido == null || !SIDO_NAMES.contains(sido)) {
            throw new BusinessException(RegionGuideErrorCode.REGION_GUIDE_INVALID_SIDO);
        }
        if (category == null || content == null || content.isBlank()) {
            throw new BusinessException(RegionGuideErrorCode.REGION_GUIDE_INVALID_INPUT);
        }
        boolean placeBlank = placeName == null || placeName.isBlank();
        if (category != RegionGuideCategory.NOTICE && placeBlank) {
            throw new BusinessException(RegionGuideErrorCode.REGION_GUIDE_INVALID_INPUT);
        }
        this.sido = sido;
        this.sigungu = blankToNull(sigungu);
        this.placeName = blankToNull(placeName);
        this.category = category;
        this.content = content.strip();
        this.aliases = blankToNull(aliases);
        this.sortOrder = sortOrder;
        this.updatedBy = updatedBy;
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.strip();
    }

    public Long getId() { return id; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }
    public String getPlaceName() { return placeName; }
    public RegionGuideCategory getCategory() { return category; }
    public String getContent() { return content; }
    public String getAliases() { return aliases; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedAt() { return updatedAt; }
}
