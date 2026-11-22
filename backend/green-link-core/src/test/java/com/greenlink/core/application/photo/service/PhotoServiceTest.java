package com.greenlink.core.application.photo.service;

import com.greenlink.core.application.photo.dto.PhotoResult;
import com.greenlink.core.application.photo.dto.UploadPhotoCommand;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.photo.model.Photo;
import com.greenlink.core.domain.photo.model.PhotoType;
import com.greenlink.core.domain.photo.port.PhotoRepository;
import com.greenlink.core.domain.photo.port.PhotoStorage;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock PhotoRepository photoRepository;
    @Mock PhotoStorage photoStorage;
    @Mock OrderRepository orderRepository;
    @Mock Order order;

    PhotoService service;

    @BeforeEach
    void setUp() {
        service = new PhotoService(photoRepository, photoStorage, orderRepository);
    }

    private UploadPhotoCommand command() {
        return new UploadPhotoCommand(1L, "a.jpg", new byte[]{1}, "image/jpeg", "SITE");
    }

    @Test
    void uploadPhoto_onDeletedOrder_isRejectedBeforeStoring() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(order.isDeleted()).willReturn(true);

        assertThatThrownBy(() -> service.uploadPhoto(command()))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(photoStorage, never()).store(any(), any(), any(), any());
        verify(photoRepository, never()).save(any());
    }

    @Test
    void uploadPhoto_onNormalOrder_stores() {
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(order.isDeleted()).willReturn(false);
        given(photoStorage.store(any(), any(), any(), any())).willReturn(new PhotoStorage.UploadResult("key", "http://u"));
        given(photoRepository.save(any(Photo.class))).willAnswer(inv -> inv.getArgument(0));

        PhotoResult result = service.uploadPhoto(command());

        assertThat(result.photoUrl()).isEqualTo("http://u");
        verify(photoStorage).store(any(), any(), any(), any());
    }

    @Test
    void selectPhoto_onDeletedOrder_isRejected() {
        Photo photo = Photo.create(1L, "key", "http://u", PhotoType.SITE);
        given(photoRepository.findById(5L)).willReturn(Optional.of(photo));
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(order.isDeleted()).willReturn(true);

        assertThatThrownBy(() -> service.selectPhoto(5L, true))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(photoRepository, never()).save(any());
    }

    @Test
    void selectPhoto_onNormalOrder_toggles() {
        Photo photo = Photo.create(1L, "key", "http://u", PhotoType.SITE);
        given(photoRepository.findById(5L)).willReturn(Optional.of(photo));
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(order.isDeleted()).willReturn(false);
        given(photoRepository.save(any(Photo.class))).willAnswer(inv -> inv.getArgument(0));

        PhotoResult result = service.selectPhoto(5L, true);

        assertThat(result.isSendToCustomer()).isTrue();
    }
}
