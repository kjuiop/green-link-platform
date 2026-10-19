package com.greenlink.admin.adapter.out.persistence.photo;

import com.greenlink.core.domain.photo.model.Photo;
import com.greenlink.core.domain.photo.model.PhotoType;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PhotoRepositoryAdapter implements PhotoRepository {

    private final PhotoJpaRepository jpaRepository;

    public PhotoRepositoryAdapter(PhotoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Photo save(Photo photo) {
        return jpaRepository.save(PhotoJpaEntity.from(photo)).toDomain();
    }

    @Override
    public Optional<Photo> findById(Long id) {
        return jpaRepository.findById(id).map(PhotoJpaEntity::toDomain);
    }

    @Override
    public List<Photo> findAllByOrderId(Long orderId) {
        return jpaRepository.findAllByOrderId(orderId).stream()
                .map(PhotoJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrderIdAndPhotoType(Long orderId, PhotoType photoType) {
        return jpaRepository.existsByOrderIdAndPhotoType(orderId, photoType);
    }
}
