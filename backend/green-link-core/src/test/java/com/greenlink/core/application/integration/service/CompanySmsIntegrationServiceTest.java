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

    private SaveCompanySmsIntegrationCommand command(String corpNum) {
        return new SaveCompanySmsIntegrationCommand(10L, "BAROBILL", "KEY", "031-111-1111", "baro_id", corpNum,
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
    void 신규_계정도_사업자번호에서_하이픈을_제거해_저장한다() {
        given(repository.findByCompanyId(10L)).willReturn(Optional.empty());

        assertThat(savedAfter(command("476-86-02607")).getCorpNum()).isEqualTo("4768602607");
    }

    @Test
    void 기존_계정_수정_시에도_하이픈을_제거해_저장한다() {
        given(repository.findByCompanyId(10L)).willReturn(Optional.of(
                CompanyIntegration.create(10L, "BAROBILL", "KEY", "031-111-1111", "baro_id", "1111111111")));

        assertThat(savedAfter(command("476-86-02607")).getCorpNum()).isEqualTo("4768602607");
    }
}
