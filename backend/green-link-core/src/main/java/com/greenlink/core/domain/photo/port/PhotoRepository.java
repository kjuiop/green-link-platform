package com.greenlink.core.domain.photo.port;

import com.greenlink.core.domain.photo.model.Photo;
import com.greenlink.core.domain.photo.model.PhotoType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PhotoRepository {

    Photo save(Photo photo);

    Optional<Photo> findById(Long id);

    List<Photo> findAll();

    List<Photo> findAllByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    boolean existsByOrderIdAndPhotoType(Long orderId, PhotoType photoType);

    Set<Long> findOrderIdsWithSitePhotoIn(Collection<Long> orderIds);

    List<Photo> findSelectedByOrderId(Long orderId);

    List<Photo> findRecentByVendorId(Long vendorId, int limit);
}
