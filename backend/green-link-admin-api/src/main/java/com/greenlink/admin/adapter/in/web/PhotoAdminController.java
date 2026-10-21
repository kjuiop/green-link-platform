package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.photo.dto.PhotoResult;
import com.greenlink.core.application.photo.usecase.GetPhotosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoAdminController {

    private final GetPhotosUseCase getPhotosUseCase;

    public PhotoAdminController(GetPhotosUseCase getPhotosUseCase) {
        this.getPhotosUseCase = getPhotosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PhotoResult>> getAll() {
        return ResponseEntity.ok(getPhotosUseCase.getAllPhotos());
    }
}
