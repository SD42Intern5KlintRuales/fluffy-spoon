package com.example.excelvalidator.service.engine;

import com.example.excelvalidator.model.CellValidationResults;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkbookRuleEngine {

    public void validateRequiredSheets(
            Workbook workbook,
            FileRuleConfig fileConfig,
            List<CellValidationResults> errors,
            List<CellValidationResults> passedFields
    ) {

        for (String requiredSheet :
                fileConfig.getRequiredSheets()) {

            if (
                    workbook.getSheet(requiredSheet)
                            == null
            ) {

                errors.add(
                        new CellValidationResults(
                                "Workbook",
                                "",
                                requiredSheet,
                                "",
                                "Required sheet is missing"
                        )
                );
            } else {
                passedFields.add(
                        new CellValidationResults(
                                "Workbook",
                                "",
                                requiredSheet,
                                "",
                                "Required sheet is present"
                        )
                );
            }
        }
    }
}