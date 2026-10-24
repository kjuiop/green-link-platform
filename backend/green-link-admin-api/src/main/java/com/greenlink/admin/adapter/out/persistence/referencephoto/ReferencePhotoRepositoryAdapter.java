package com.greenlink.admin.adapter.out.persistence.referencephoto;

import com.greenlink.core.domain.referencephoto.model.ReferencePhoto;
import com.greenlink.core.domain.referencephoto.port.ReferencePhotoRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReferencePhotoRepositoryAdapter implements ReferencePhotoRepository {

    private final ReferencePhotoJpaRepository jpaRepository;

    public ReferencePhotoRepositoryAdapter(ReferencePhotoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReferencePhoto save(ReferencePhoto photo) {
        return jpaRepository.save(ReferencePhotoJpaEntity.from(photo)).toDomain();
    }

    @Override
    public List<ReferencePhoto> findAllByOrderId(Long orderId) {
        return jpaRepository.findAllByOrderId(orderId).stream()
                .map(ReferencePhotoJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Map<Long, Long> countByOrderIds(List<Long> orderIds) {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : jpaRepository.countByOrderIds(orderIds)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }
}
