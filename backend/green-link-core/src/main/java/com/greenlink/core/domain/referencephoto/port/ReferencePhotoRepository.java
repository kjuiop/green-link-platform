package com.greenlink.core.domain.referencephoto.port;

import com.greenlink.core.domain.referencephoto.model.ReferencePhoto;

import java.util.List;
import java.util.Map;

public interface ReferencePhotoRepository {

    ReferencePhoto save(ReferencePhoto photo);

    List<ReferencePhoto> findAllByOrderId(Long orderId);

    Map<Long, Long> countByOrderIds(List<Long> orderIds);
}
