package com.example.excelvalidator.controller;

import com.example.excelvalidator.model.response.BatchValidationResponse;
import com.example.excelvalidator.service.ExcelValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExcelControllerMultipartCompatibilityTest {

    @Test
    void validateBatch_acceptsFrontendFieldNames() throws Exception {
        ExcelValidationService excelValidationService = mock(ExcelValidationService.class);
        ExcelController controller = new ExcelController(excelValidationService);

        MockMultipartFile excelFile = new MockMultipartFile(
                "excelFiles",
                "sample.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy".getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile rulesFile = new MockMultipartFile(
                "rulesFile",
                "validation-rules.json",
                "application/json",
                "{}".getBytes(StandardCharsets.UTF_8)
        );

        BatchValidationResponse response = new BatchValidationResponse();
        when(excelValidationService.validateBatch(any(List.class), any(MultipartFile.class))).thenReturn(response);

        ResponseEntity<BatchValidationResponse> actualResponse = controller.validateBatch(
                List.of(excelFile),
                rulesFile
        );

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<List<MultipartFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<MultipartFile> rulesCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(excelValidationService).validateBatch(filesCaptor.capture(), rulesCaptor.capture());

        assertThat(filesCaptor.getValue()).hasSize(1);
        assertThat(rulesCaptor.getValue()).isNotNull();
        assertThat(rulesCaptor.getValue().getOriginalFilename()).isEqualTo("validation-rules.json");
    }
}
