package com.greenlink.admin.adapter.out.persistence.regionguide;

import com.greenlink.core.domain.regionguide.model.RegionGuide;
import com.greenlink.core.domain.regionguide.port.RegionGuideRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RegionGuideRepositoryAdapter implements RegionGuideRepository {

    private final RegionGuideJpaRepository jpaRepository;

    public RegionGuideRepositoryAdapter(RegionGuideJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RegionGuide save(RegionGuide guide) {
        return jpaRepository.save(RegionGuideJpaEntity.from(guide)).toDomain();
    }

    @Override
    public Optional<RegionGuide> findById(Long id) {
        return jpaRepository.findById(id).map(RegionGuideJpaEntity::toDomain);
    }

    @Override
    public List<RegionGuide> findAllActive() {
        return jpaRepository.findAllByActiveTrueOrderBySidoAscSortOrderAscIdAsc().stream()
                .map(RegionGuideJpaEntity::toDomain).toList();
    }

    @Override
    public List<RegionGuide> findBySido(String sido) {
        return jpaRepository.findAllBySidoOrderBySortOrderAscIdAsc(sido).stream()
                .map(RegionGuideJpaEntity::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
