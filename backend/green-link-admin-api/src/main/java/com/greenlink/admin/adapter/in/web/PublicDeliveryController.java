package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.DeliveryShareResult;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.photo.model.Photo;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/delivery")
public class PublicDeliveryController {

    private final OrderRepository orderRepository;
    private final PhotoRepository photoRepository;

    public PublicDeliveryController(OrderRepository orderRepository, PhotoRepository photoRepository) {
        this.orderRepository = orderRepository;
        this.photoRepository = photoRepository;
    }

    @GetMapping("/{token}")
    public ResponseEntity<DeliveryShareResult> getDeliveryShare(@PathVariable String token) {
        Order order = orderRepository.findByPhotoToken(token)
                .orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> photoUrls = photoRepository.findSelectedByOrderId(order.getId()).stream()
                .map(Photo::getPhotoUrl)
                .toList();

        DeliveryShareResult result = new DeliveryShareResult(
                order.getRecipientName(),
                order.getPlaceName(),
                order.getCompletedAt(),
                photoUrls
        );

        return ResponseEntity.ok(result);
    }
}
