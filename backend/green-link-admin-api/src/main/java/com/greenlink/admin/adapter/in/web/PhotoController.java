package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.photo.dto.PhotoResult;
import com.greenlink.core.application.photo.dto.UploadPhotoCommand;
import com.greenlink.core.application.photo.usecase.GetPhotosUseCase;
import com.greenlink.core.application.photo.usecase.SelectPhotoUseCase;
import com.greenlink.core.application.photo.usecase.UploadPhotoUseCase;
import com.greenlink.core.global.exception.BusinessException;
import com.greenlink.core.global.exception.photo.PhotoErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/{orderId}/photos")
public class PhotoController {

    private final UploadPhotoUseCase uploadPhotoUseCase;
    private final SelectPhotoUseCase selectPhotoUseCase;
    private final GetPhotosUseCase getPhotosUseCase;

    public PhotoController(UploadPhotoUseCase uploadPhotoUseCase,
                           SelectPhotoUseCase selectPhotoUseCase,
                           GetPhotosUseCase getPhotosUseCase) {
        this.uploadPhotoUseCase = uploadPhotoUseCase;
        this.selectPhotoUseCase = selectPhotoUseCase;
        this.getPhotosUseCase = getPhotosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PhotoResult>> getAll(@PathVariable Long orderId) {
        return ResponseEntity.ok(getPhotosUseCase.getPhotosByOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<List<PhotoResult>> upload(
            @PathVariable Long orderId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("photoType") String photoType) {

        List<PhotoResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                UploadPhotoCommand command = new UploadPhotoCommand(
                        orderId,
                        file.getOriginalFilename(),
                        file.getBytes(),
                        file.getContentType(),
                        photoType
                );
                results.add(uploadPhotoUseCase.uploadPhoto(command));
            } catch (IOException e) {
                throw new BusinessException(PhotoErrorCode.PHOTO_UPLOAD_FAILED);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @PutMapping("/{photoId}/select")
    public ResponseEntity<PhotoResult> select(
            @PathVariable Long orderId,
            @PathVariable Long photoId,
            @RequestBody Map<String, Boolean> body) {
        boolean selected = Boolean.TRUE.equals(body.get("selected"));
        return ResponseEntity.ok(selectPhotoUseCase.selectPhoto(photoId, selected));
    }
}
