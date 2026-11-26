package com.greenlink.admin.adapter.in.web;

import com.greenlink.admin.adapter.in.web.dto.ApproveOnurimApplicationRequest;
import com.greenlink.admin.adapter.in.web.dto.RejectOnurimApplicationRequest;
import com.greenlink.core.application.common.dto.PageResult;
import com.greenlink.core.application.onurimapplication.dto.OnurimApplicationResult;
import com.greenlink.core.application.onurimapplication.usecase.ManageOnurimApplicationUseCase;
import com.greenlink.core.domain.onurimapplication.model.OnurimApplicationStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/onurim-applications")
public class OnurimApplicationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ManageOnurimApplicationUseCase useCase;

    public OnurimApplicationController(ManageOnurimApplicationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<PageResult<OnurimApplicationResult>> list(
            @RequestParam(defaultValue = "PENDING") OnurimApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
            Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(PageResult.of(useCase.list(status, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnurimApplicationResult> get(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.get(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OnurimApplicationResult> approve(@PathVariable Long id,
                                                           @RequestBody @Valid ApproveOnurimApplicationRequest req,
                                                           Authentication authentication) {
        return ResponseEntity.ok(useCase.approve(id, req.type(), Long.parseLong(authentication.getName())));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OnurimApplicationResult> reject(@PathVariable Long id,
                                                          @RequestBody @Valid RejectOnurimApplicationRequest req,
                                                          Authentication authentication) {
        return ResponseEntity.ok(useCase.reject(id, req.reason(), Long.parseLong(authentication.getName())));
    }
}
