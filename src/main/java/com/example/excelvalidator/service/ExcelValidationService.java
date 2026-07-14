package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.service.validator.SheetValidator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelValidationService {

    private final List<SheetValidator> validators;

    public ExcelValidationService(
            List<SheetValidator> validators
    ) {
        this.validators = validators;
    }

    public ExcelValidationResponse validate(
            MultipartFile file
    ) {
        List<String> sheets = new ArrayList<>();
        List<CellValidationError> errors =
                new ArrayList<>();

        int rowsChecked = 0;

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                file.getInputStream()
                        )
        ) {

            DataFormatter formatter =
                    new DataFormatter();

            for (
                    int sheetIndex = 0;
                    sheetIndex < workbook.getNumberOfSheets();
                    sheetIndex++
            ) {

                Sheet sheet =
                        workbook.getSheetAt(sheetIndex);

                sheets.add(sheet.getSheetName());

                SheetValidator validator =
                        findValidator(
                                sheet.getSheetName()
                        );

                if (validator == null) {

                    System.out.println(
                            "No validator found for sheet: "
                                    + sheet.getSheetName()
                    );

                    continue;
                }

                rowsChecked += validator.validate(
                        sheet,
                        formatter,
                        errors
                );
            }

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error reading Excel file",
                    ex
            );
        }

        return new ExcelValidationResponse(
                file.getOriginalFilename(),
                errors.isEmpty(),
                rowsChecked,
                errors.size(),
                sheets,
                errors
        );
    }

    private SheetValidator findValidator(
            String sheetName
    ) {

        return validators.stream()
                .filter(
                        validator ->
                                validator.sheetName()
                                        .trim()
                                        .equalsIgnoreCase(
                                                sheetName.trim()
                                        )
                )
                .findFirst()
                .orElse(null);
    }
}