package com.greenlink.core.domain.order.port;

import com.greenlink.core.domain.order.model.OrderSearchCondition;
import com.greenlink.core.domain.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(Long id);

    Optional<Order> findByIdForUpdate(Long id);

    Optional<Order> findByPhotoToken(String photoToken);

    List<Order> findAllByIds(List<Long> ids);

    List<Order> search(OrderSearchCondition condition);

    Page<Order> searchPaged(OrderSearchCondition condition, Pageable pageable);

    long sumAmount(OrderSearchCondition condition);

    long sumVendorFee(OrderSearchCondition condition);

    Order save(Order order);
}
