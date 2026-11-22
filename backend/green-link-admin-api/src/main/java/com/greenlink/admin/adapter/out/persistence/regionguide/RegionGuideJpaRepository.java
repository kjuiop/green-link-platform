package com.greenlink.admin.adapter.out.persistence.regionguide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionGuideJpaRepository extends JpaRepository<RegionGuideJpaEntity, Long> {

    List<RegionGuideJpaEntity> findAllByActiveTrueOrderBySidoAscSortOrderAscIdAsc();

    List<RegionGuideJpaEntity> findAllBySidoOrderBySortOrderAscIdAsc(String sido);
}
