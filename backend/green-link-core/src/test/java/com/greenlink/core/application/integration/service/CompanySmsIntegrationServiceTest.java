package com.greenlink.core.application.integration.service;

import com.greenlink.core.application.integration.dto.SaveCompanySmsIntegrationCommand;
import com.greenlink.core.domain.integration.model.CompanyIntegration;
import com.greenlink.core.domain.integration.port.CompanyIntegrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanySmsIntegrationServiceTest {

    @Mock CompanyIntegrationRepository repository;

    private SaveCompanySmsIntegrationCommand command(String apiKey) {
        return new SaveCompanySmsIntegrationCommand(10L, "BAROBILL", apiKey, "031-111-1111", "baro_id",
                false, null, null, null, null);
    }

    private CompanyIntegration savedAfter(SaveCompanySmsIntegrationCommand command) {
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));
        new CompanySmsIntegrationService(repository).save(command);
        ArgumentCaptor<CompanyIntegration> saved = ArgumentCaptor.forClass(CompanyIntegration.class);
        verify(repository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    void 신규_계정은_입력값_그대로_저장한다() {
        given(repository.findByCompanyId(10L)).willReturn(Optional.empty());

        CompanyIntegration saved = savedAfter(command("NEW-KEY"));

        assertThat(saved.getApiKey()).isEqualTo("NEW-KEY");
        assertThat(saved.getUserId()).isEqualTo("baro_id");
        assertThat(saved.getSenderNumber()).isEqualTo("031-111-1111");
    }

    @Test
    void 마스킹된_키가_다시_저장되면_기존_키를_유지한다() {
        given(repository.findByCompanyId(10L)).willReturn(Optional.of(
                CompanyIntegration.create(10L, "BAROBILL", "ABCDEFGH-1234", "031-111-1111", "baro_id")));

        assertThat(savedAfter(command("****1234")).getApiKey()).isEqualTo("ABCDEFGH-1234");
    }
}
