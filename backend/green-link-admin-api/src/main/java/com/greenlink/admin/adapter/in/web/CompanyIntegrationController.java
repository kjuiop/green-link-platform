package com.greenlink.admin.adapter.in.web;

import com.greenlink.core.application.integration.dto.CompanySmsIntegrationResult;
import com.greenlink.core.application.integration.dto.SaveCompanySmsIntegrationCommand;
import com.greenlink.core.application.integration.usecase.GetCompanySmsIntegrationUseCase;
import com.greenlink.core.application.integration.usecase.SaveCompanySmsIntegrationUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/integrations/sms")
public class CompanyIntegrationController {

    private final GetCompanySmsIntegrationUseCase getUseCase;
    private final SaveCompanySmsIntegrationUseCase saveUseCase;

    public CompanyIntegrationController(GetCompanySmsIntegrationUseCase getUseCase,
                                        SaveCompanySmsIntegrationUseCase saveUseCase) {
        this.getUseCase = getUseCase;
        this.saveUseCase = saveUseCase;
    }

    @GetMapping
    public ResponseEntity<CompanySmsIntegrationResult> get(@PathVariable Long companyId) {
        return getUseCase.get(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping
    public ResponseEntity<CompanySmsIntegrationResult> save(@PathVariable Long companyId,
                                                            @RequestBody SaveCompanySmsIntegrationCommand command) {
        SaveCompanySmsIntegrationCommand cmd = new SaveCompanySmsIntegrationCommand(
                companyId, command.provider(), command.apiKey(), command.senderNumber());
        return ResponseEntity.ok(saveUseCase.save(cmd));
    }
}
