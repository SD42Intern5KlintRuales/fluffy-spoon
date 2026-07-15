package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkbookRuleEngine {

    public void validateRequiredSheets(
            Workbook workbook,
            FileRuleConfig fileConfig,
            List<CellValidationError> errors
    ) {

        for (String requiredSheet :
                fileConfig.getRequiredSheets()) {

            if (
                    workbook.getSheet(requiredSheet)
                            == null
            ) {

                errors.add(
                        new CellValidationError(
                                "Workbook",
                                0,
                                "",
                                requiredSheet,
                                "",
                                "Required sheet is missing"
                        )
                );
            }
        }
    }
}