package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.ChangeVendorPasswordRequest;
import com.greenlink.admin.adapter.in.web.dto.CreateVendorRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateVendorRequest;
import com.greenlink.admin.adapter.in.web.dto.UpdateVendorStatusRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.photo.dto.PhotoResult;
import com.greenlink.core.application.photo.usecase.GetPhotosUseCase;
import com.greenlink.core.application.vendor.dto.VendorResult;
import com.greenlink.core.application.vendor.usecase.ChangeVendorPasswordUseCase;
import com.greenlink.core.application.vendor.usecase.CreateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.DeleteVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorUseCase;
import com.greenlink.core.application.vendor.usecase.GetVendorsUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorStatusUseCase;
import com.greenlink.core.application.vendor.usecase.UpdateVendorUseCase;
import com.greenlink.core.application.vendor.usecase.UploadVendorDocumentUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final GetVendorsUseCase getVendorsUseCase;
    private final GetVendorUseCase getVendorUseCase;
    private final CreateVendorUseCase createVendorUseCase;
    private final UpdateVendorUseCase updateVendorUseCase;
    private final UpdateVendorStatusUseCase updateVendorStatusUseCase;
    private final DeleteVendorUseCase deleteVendorUseCase;
    private final GetPhotosUseCase getPhotosUseCase;
    private static final Set<String> ALLOWED_DOC_TYPES = Set.of("businessReg", "bankBook");

    private final UploadVendorDocumentUseCase uploadVendorDocumentUseCase;
    private final ChangeVendorPasswordUseCase changeVendorPasswordUseCase;

    public VendorController(GetVendorsUseCase getVendorsUseCase,
                            GetVendorUseCase getVendorUseCase,
                            CreateVendorUseCase createVendorUseCase,
                            UpdateVendorUseCase updateVendorUseCase,
                            UpdateVendorStatusUseCase updateVendorStatusUseCase,
                            DeleteVendorUseCase deleteVendorUseCase,
                            GetPhotosUseCase getPhotosUseCase,
                            UploadVendorDocumentUseCase uploadVendorDocumentUseCase,
                            ChangeVendorPasswordUseCase changeVendorPasswordUseCase) {
        this.getVendorsUseCase = getVendorsUseCase;
        this.getVendorUseCase = getVendorUseCase;
        this.createVendorUseCase = createVendorUseCase;
        this.updateVendorUseCase = updateVendorUseCase;
        this.updateVendorStatusUseCase = updateVendorStatusUseCase;
        this.deleteVendorUseCase = deleteVendorUseCase;
        this.getPhotosUseCase = getPhotosUseCase;
        this.uploadVendorDocumentUseCase = uploadVendorDocumentUseCase;
        this.changeVendorPasswordUseCase = changeVendorPasswordUseCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<VendorResult>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(getVendorsUseCase.getVendorsPaged(status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResult> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(getVendorUseCase.getVendor(id));
    }

    @PostMapping
    public ResponseEntity<VendorResult> create(@RequestBody @Valid CreateVendorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(createVendorUseCase.createVendor(req.toCommand()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorResult> update(@PathVariable Long id,
                                               @RequestBody @Valid UpdateVendorRequest req) {
        return ResponseEntity.ok(updateVendorUseCase.updateVendor(id, req.toCommand()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<VendorResult> updateStatus(@PathVariable Long id,
                                                     @RequestBody @Valid UpdateVendorStatusRequest req) {
        return ResponseEntity.ok(updateVendorStatusUseCase.updateVendorStatus(id, req.action()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteVendorUseCase.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadVendorDocumentUseCase.Result> uploadDocument(
            @PathVariable Long id,
            @RequestParam("type") String docType,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (!ALLOWED_DOC_TYPES.contains(docType)) {
            throw new IllegalArgumentException("지원하지 않는 문서 유형입니다: " + docType);
        }
        return ResponseEntity.ok(
                uploadVendorDocumentUseCase.uploadDocument(
                        id, docType, file.getOriginalFilename(), file.getBytes(), file.getContentType()));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @RequestBody @Valid ChangeVendorPasswordRequest req) {
        changeVendorPasswordUseCase.changePassword(id, req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{vendorId}/recent-photos")
    public ResponseEntity<List<PhotoResult>> getRecentPhotos(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(getPhotosUseCase.getRecentPhotosByVendor(vendorId, limit));
    }
}
