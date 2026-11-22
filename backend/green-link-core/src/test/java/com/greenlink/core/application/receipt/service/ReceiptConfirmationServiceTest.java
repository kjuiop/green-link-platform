package com.greenlink.core.application.receipt.service;

import com.greenlink.core.application.orderhistory.usecase.RecordOrderHistoryUseCase;
import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand;
import com.greenlink.core.application.receipt.dto.UpdateReceiptInfoCommand;
import com.greenlink.core.domain.order.model.Order;
import com.greenlink.core.domain.order.port.OrderRepository;
import com.greenlink.core.domain.photo.port.PhotoStorage;
import com.greenlink.core.domain.receipt.model.ReceiptConfirmation;
import com.greenlink.core.domain.receipt.port.ReceiptConfirmationRepository;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.order.OrderErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReceiptConfirmationServiceTest {

    @Mock ReceiptConfirmationRepository repository;
    @Mock PhotoStorage photoStorage;
    @Mock RecordOrderHistoryUseCase recordOrderHistoryUseCase;
    @Mock OrderRepository orderRepository;
    @Mock Order order;

    ReceiptConfirmationService service;

    @BeforeEach
    void setUp() {
        service = new ReceiptConfirmationService(repository, photoStorage, recordOrderHistoryUseCase, orderRepository);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
    }

    private SaveReceiptConfirmationCommand saveCommand() {
        return new SaveReceiptConfirmationCommand(1L, "홍길동", "본인", true, null, LocalDateTime.now(),
                List.of(new SaveReceiptConfirmationCommand.PhotoFile("a.jpg", new byte[]{1}, "image/jpeg")));
    }

    private ReceiptConfirmation receipt() {
        return ReceiptConfirmation.create(1L, "홍길동", "본인", true, null, LocalDateTime.now(),
                List.of("http://u"), List.of());
    }

    @Test
    void save_onDeletedOrder_isRejectedBeforeStoring() {
        given(order.isDeleted()).willReturn(true);

        assertThatThrownBy(() -> service.save(saveCommand()))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(photoStorage, never()).store(any(), any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void addPhotos_onDeletedOrder_isRejectedBeforeStoring() {
        given(order.isDeleted()).willReturn(true);

        assertThatThrownBy(() -> service.addPhotos(1L, List.of(new SaveReceiptConfirmationCommand.PhotoFile("a.jpg", new byte[]{1}, "image/jpeg"))))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(photoStorage, never()).store(any(), any(), any(), any());
        verify(repository, never()).appendPhotos(anyLong(), any(), any());
    }

    @Test
    void updateInfo_onDeletedOrder_isRejected() {
        given(order.isDeleted()).willReturn(true);

        UpdateReceiptInfoCommand cmd = new UpdateReceiptInfoCommand(1L, "홍길동", "본인", true, null, LocalDateTime.now());
        assertThatThrownBy(() -> service.updateInfo(cmd))
                .isInstanceOf(BusinessException.class).hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        verify(repository, never()).updateReceiverInfo(any(), any(), any(), any(), any(), any());
    }

    @Test
    void save_onNormalOrder_savesAndRecordsHistory() {
        given(order.isDeleted()).willReturn(false);
        given(photoStorage.store(any(), any(), any(), any())).willReturn(new PhotoStorage.UploadResult("key", "http://u"));
        given(repository.save(any(ReceiptConfirmation.class))).willAnswer(inv -> inv.getArgument(0));

        ReceiptConfirmationResult result = service.save(saveCommand());

        assertThat(result.sitePhotoUrls()).containsExactly("http://u");
        verify(recordOrderHistoryUseCase).record(eq(1L), any(), any());
    }

    @Test
    void addPhotos_onNormalOrder_appendsPhotos() {
        given(order.isDeleted()).willReturn(false);
        given(photoStorage.store(any(), any(), any(), any())).willReturn(new PhotoStorage.UploadResult("key", "http://u"));
        given(repository.appendPhotos(eq(1L), any(), eq("SITE"))).willReturn(receipt());

        service.addPhotos(1L, List.of(new SaveReceiptConfirmationCommand.PhotoFile("a.jpg", new byte[]{1}, "image/jpeg")));

        verify(repository).appendPhotos(eq(1L), any(), eq("SITE"));
    }

    @Test
    void updateInfo_onNormalOrder_updates() {
        given(order.isDeleted()).willReturn(false);
        given(repository.updateReceiverInfo(any(), any(), any(), any(), any(), any())).willReturn(receipt());

        service.updateInfo(new UpdateReceiptInfoCommand(1L, "홍길동", "본인", true, null, LocalDateTime.now()));

        verify(repository).updateReceiverInfo(any(), any(), any(), any(), any(), any());
    }
}
