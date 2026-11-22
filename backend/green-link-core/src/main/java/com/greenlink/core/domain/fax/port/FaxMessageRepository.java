package com.greenlink.core.domain.fax.port;

import com.greenlink.core.domain.fax.model.FaxMessage;
import com.greenlink.core.domain.fax.model.FaxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FaxMessageRepository {

    Optional<FaxMessage> findById(Long id);

    Optional<FaxMessage> findLatestEffectiveByOrderId(Long orderId);

    int findMaxSequence(Long orderId);

    /** (order_id, sequence) 유니크 제약 충돌 시 empty를 반환한다. */
    Optional<FaxMessage> saveIfAbsent(FaxMessage message);

    FaxMessage save(FaxMessage message);

    List<FaxMessage> findByStatus(FaxStatus status, int limit);

    Page<FaxMessage> search(FaxStatus status, String keyword, Pageable pageable);
}
