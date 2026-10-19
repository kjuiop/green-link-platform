package com.greenlink.core.domain.photo.port;

import com.greenlink.core.domain.photo.model.Photo;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository {

    Photo save(Photo photo);

    Optional<Photo> findById(Long id);

    List<Photo> findAllByOrderId(Long orderId);
}
