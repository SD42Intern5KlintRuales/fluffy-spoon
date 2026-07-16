package com.example.excelvalidator;

import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.service.ExcelValidationService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ExcelValidationIntegrationTest {

    @Autowired
    private ExcelValidationService excelValidationService;

    @Test
    public void validateReleaseNotes_passesWhenAllRequiredPresent() throws Exception {
        // load rules JSON
        ClassPathResource rulesResource = new ClassPathResource("validation-rules.json");
        try (InputStream rulesStream = rulesResource.getInputStream()) {
            MockMultipartFile rulesFile = new MockMultipartFile(
                    "rules",
                    "validation-rules.json",
                    "application/json",
                    rulesStream
            );

            // build an in-memory workbook that satisfies the RELEASE_NOTES rules
            XSSFWorkbook workbook = new XSSFWorkbook();

            // ReleaseNote sheet
            var rn = workbook.createSheet("ReleaseNote");
            Row r2 = rn.createRow(1); // E2 -> row index 1
            r2.createCell(4).setCellValue("Project X");

            Row r3 = rn.createRow(2); // E3
            r3.createCell(4).setCellValue("Purpose");

            Row r4 = rn.createRow(3); // E4 date
            Cell dateCell = r4.createCell(4);
            dateCell.setCellValue(new Date());
            CellStyle dateStyle = workbook.createCellStyle();
            short df = workbook.createDataFormat().getFormat("yyyy-mm-dd");
            dateStyle.setDataFormat(df);
            dateCell.setCellStyle(dateStyle);

            // set other simple required fields
            Row rL3 = rn.getRow(2); if (rL3 == null) rL3 = rn.createRow(2);
            rL3.createCell(11).setCellValue("Prepared By"); // L3
            rL3.createCell(14).setCellValue("Confirmed By"); // O3
            rL3.createCell(17).setCellValue("Approved By"); // R3

            // populate other required ReleaseNote cells (B12, L12, B14, B16, B18, B20)
            Row row12 = rn.createRow(11); // row 12
            row12.createCell(1).setCellValue("branch-name"); // B12
            row12.createCell(11).setCellValue("1.2.3"); // L12

            Row row14 = rn.createRow(13); // row 14
            row14.createCell(1).setCellValue("List of changes"); // B14

            Row row16 = rn.createRow(15); // row 16
            row16.createCell(1).setCellValue("English"); // B16

            Row row18 = rn.createRow(17); // row 18
            row18.createCell(1).setCellValue("Linux"); // B18

            Row row20 = rn.createRow(19); // row 20
            row20.createCell(1).setCellValue("None"); // B20

            // ReleaseDetailsHistory sheet
            var rdh = workbook.createSheet("ReleaseDetailsHistory");
            Row rdRow3 = rdh.createRow(2); // C3
            Cell rdDate = rdRow3.createCell(2);
            rdDate.setCellValue(new Date());
            rdDate.setCellStyle(dateStyle);

            Row rdRow4 = rdh.createRow(3); // C4
            rdRow4.createCell(2).setCellValue("1.0.0");

            // populate C5, C7, C8 in ReleaseDetailsHistory
            Row rdRow5 = rdh.createRow(4); // C5
            rdRow5.createCell(2).setCellValue("Implemented feature X");

            Row rdRow7 = rdh.createRow(6); // C7
            rdRow7.createCell(2).setCellValue("No known defects");

            Row rdRow8 = rdh.createRow(7); // C8
            rdRow8.createCell(2).setCellValue("Notes here");

            // Product_ReleaseCheckpoints sheet with one valid row
            var prc = workbook.createSheet("Product_ReleaseCheckpoints");
            // header rows up to startRow-1
            for (int i = 0; i < 3; i++) prc.createRow(i);
            Row start = prc.createRow(3); // startRow=4 -> index 3
            start.createCell(1).setCellValue("Category A"); // B
            start.createCell(2).setCellValue("Checkpoint 1"); // C
            start.createCell(3).setCellValue("OK"); // D

            // write workbook to bytes
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();

            MockMultipartFile excelFile = new MockMultipartFile(
                    "file",
                    "release-notes.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bos.toByteArray()
            );

            ExcelValidationResponse resp = excelValidationService.validate(
                    excelFile,
                    rulesFile,
                    "RELEASE_NOTES"
            );

            assertThat(resp).isNotNull();
            assertThat(resp.valid()).withFailMessage(() -> "Validation errors: " + resp.errors()).isTrue();
            assertThat(resp.errors()).isEmpty();
        }
    }
}
