package com.example.excelvalidator;

import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.service.ExcelValidationService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ExcelValidationTableFailureTest {

    @Autowired
    private ExcelValidationService excelValidationService;

    @Test
    public void testSpecification_tableRules_detectMissingRequiredCells() throws Exception {
        ClassPathResource rulesResource = new ClassPathResource("validation-rules.json");
        try (InputStream rulesStream = rulesResource.getInputStream()) {
            MockMultipartFile rulesFile = new MockMultipartFile(
                    "rules",
                    "validation-rules.json",
                    "application/json",
                    rulesStream
            );

            // Build workbook with TestSpecification sheet and an empty table starting at row 10
            XSSFWorkbook workbook = new XSSFWorkbook();
            var ts = workbook.createSheet("TestSpecification");

            // ensure rows up to startRow-1 exist (startRow=10)
            for (int i = 0; i < 9; i++) ts.createRow(i);

            // Create the table start row (index 9) with blank cells for required columns
            Row tableRow = ts.createRow(9);
            // leave A-E blank intentionally to trigger required field failures

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            MockMultipartFile excelFile = new MockMultipartFile(
                    "file",
                    "test-spec.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bos.toByteArray()
            );

            ExcelValidationResponse resp = excelValidationService.validate(
                    excelFile,
                    rulesFile,
                    "TEST_SPECIFICATION_RESULT"
            );

            assertThat(resp).isNotNull();
            assertThat(resp.valid()).isFalse();

            // Expect 5 required columns (A-E) to be flagged for the single table row
            assertThat(resp.failedChecks()).isEqualTo(resp.errors().size());
            List<String> cols = resp.errors().stream().map(CellValidationError::column).collect(Collectors.toList());
            // Verify at least A-E were reported
            assertThat(cols).contains("A", "B", "C", "D", "E");
        }
    }
}
