package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.referencephoto.dto.ReferencePhotoResult;
import com.greenlink.core.application.referencephoto.dto.UploadReferencePhotoCommand;
import com.greenlink.core.application.referencephoto.usecase.GetReferencePhotosUseCase;
import com.greenlink.core.application.referencephoto.usecase.UploadReferencePhotoUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/reference-photos")
public class ReferencePhotoController {

    private final UploadReferencePhotoUseCase uploadUseCase;
    private final GetReferencePhotosUseCase getUseCase;

    public ReferencePhotoController(UploadReferencePhotoUseCase uploadUseCase,
                                     GetReferencePhotosUseCase getUseCase) {
        this.uploadUseCase = uploadUseCase;
        this.getUseCase = getUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ReferencePhotoResult>> getAll(@PathVariable Long orderId) {
        return ResponseEntity.ok(getUseCase.getByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<List<ReferencePhotoResult>> upload(
            @PathVariable Long orderId,
            @RequestParam("files") List<MultipartFile> files) {

        List<ReferencePhotoResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                UploadReferencePhotoCommand command = new UploadReferencePhotoCommand(
                        orderId,
                        file.getOriginalFilename(),
                        file.getBytes(),
                        file.getContentType()
                );
                results.add(uploadUseCase.upload(command));
            } catch (IOException e) {
                throw new BusinessException(PhotoErrorCode.PHOTO_UPLOAD_FAILED);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }
}
