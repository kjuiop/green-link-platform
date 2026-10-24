package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.receipt.dto.ReceiptConfirmationResult;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand;
import com.greenlink.core.application.receipt.dto.SaveReceiptConfirmationCommand.PhotoFile;
import com.greenlink.core.application.receipt.dto.UpdateReceiptInfoCommand;
import com.greenlink.core.application.receipt.usecase.AddReceiptPhotosUseCase;
import com.greenlink.core.application.receipt.usecase.GetReceiptConfirmationUseCase;
import com.greenlink.core.application.receipt.usecase.SaveReceiptConfirmationUseCase;
import com.greenlink.core.application.receipt.usecase.UpdateReceiptInfoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/receipt")
public class ReceiptConfirmationController {

    private final SaveReceiptConfirmationUseCase saveUseCase;
    private final GetReceiptConfirmationUseCase getUseCase;
    private final AddReceiptPhotosUseCase addPhotosUseCase;
    private final UpdateReceiptInfoUseCase updateInfoUseCase;

    public ReceiptConfirmationController(SaveReceiptConfirmationUseCase saveUseCase,
                                          GetReceiptConfirmationUseCase getUseCase,
                                          AddReceiptPhotosUseCase addPhotosUseCase,
                                          UpdateReceiptInfoUseCase updateInfoUseCase) {
        this.saveUseCase = saveUseCase;
        this.getUseCase = getUseCase;
        this.addPhotosUseCase = addPhotosUseCase;
        this.updateInfoUseCase = updateInfoUseCase;
    }

    @GetMapping
    public ResponseEntity<ReceiptConfirmationResult> get(@PathVariable Long orderId) {
        return getUseCase.getByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<ReceiptConfirmationResult> save(
            @PathVariable Long orderId,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String receiverRelation,
            @RequestParam(defaultValue = "false") boolean delivered,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) String confirmedAt,
            @RequestParam(required = false) List<MultipartFile> files) throws IOException {

        List<PhotoFile> photoFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    photoFiles.add(new PhotoFile(
                            file.getOriginalFilename(),
                            file.getBytes(),
                            file.getContentType()
                    ));
                }
            }
        }

        LocalDateTime confirmedDateTime = confirmedAt != null && !confirmedAt.isBlank()
                ? LocalDateTime.parse(confirmedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : LocalDateTime.now();

        SaveReceiptConfirmationCommand command = new SaveReceiptConfirmationCommand(
                orderId, receiverName, receiverRelation, delivered, memo, confirmedDateTime, photoFiles);

        return ResponseEntity.status(HttpStatus.CREATED).body(saveUseCase.save(command));
    }

    @PatchMapping("/info")
    public ResponseEntity<ReceiptConfirmationResult> updateInfo(
            @PathVariable Long orderId,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String receiverRelation,
            @RequestParam(required = false) Boolean delivered,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) String confirmedAt) {

        LocalDateTime confirmedDateTime = confirmedAt != null && !confirmedAt.isBlank()
                ? LocalDateTime.parse(confirmedAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;

        UpdateReceiptInfoCommand command = new UpdateReceiptInfoCommand(
                orderId, receiverName, receiverRelation, delivered, memo, confirmedDateTime);

        return ResponseEntity.ok(updateInfoUseCase.updateInfo(command));
    }

    @PostMapping("/photos")
    public ResponseEntity<ReceiptConfirmationResult> addPhotos(
            @PathVariable Long orderId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "photoType", defaultValue = "SITE") String photoType) throws IOException {

        if (!"SITE".equals(photoType) && !"STORE".equals(photoType)) {
            return ResponseEntity.badRequest().build();
        }

        List<PhotoFile> photoFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                photoFiles.add(new PhotoFile(
                        file.getOriginalFilename(),
                        file.getBytes(),
                        file.getContentType(),
                        photoType
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(addPhotosUseCase.addPhotos(orderId, photoFiles));
    }
}
