package com.example.excelvalidator.service.engine;

import com.example.excelvalidator.model.CellValidationResults;
import com.example.excelvalidator.model.validation.v2.CellRuleConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class CellRuleEngine {

    public void validateCellRules(
            Workbook workbook,
            List<CellRuleConfig> rules,
            List<CellValidationResults> errors,
            List<CellValidationResults> passedFields
    ){
        DataFormatter formatter = new DataFormatter();

        for(CellRuleConfig rule : rules){

            Sheet sheet = workbook.getSheet(rule.getSheet());

            if(sheet == null){
                continue;
            }

            validateRule(
                    sheet,
                    rule,
                    formatter,
                    errors,
                    passedFields
            );
        }
    }

    private void validateRule(
            Sheet sheet,
            CellRuleConfig rule,
            DataFormatter formatter,
            List<CellValidationResults> errors,
            List<CellValidationResults> passedFields
    ) {
        String cellAddress = rule.getCell();

        CellReference ref = new CellReference(cellAddress);

        Row row = sheet.getRow(ref.getRow());

        Cell cell = null;

        if(row != null){
            cell = row.getCell(
                    ref.getCol(),
                    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
            );
        }

        String value = "";

        if(cell != null){
            value = formatter.formatCellValue(cell).trim();
        }

        validateValue(
                sheet,
                ref,
                rule,
                value,
                cell,
                errors,
                passedFields
        );
    }

    private void validateValue(
            Sheet sheet,
            CellReference ref,
            CellRuleConfig rule,
            String value,
            Cell cell,
            List<CellValidationResults> errors,
            List<CellValidationResults> passedFields
    ){
        if(
                "REQUIRED".equalsIgnoreCase(
                        rule.getValidationType()
                )
        ){
            if(value.isBlank()){
                errors.add(
                        new CellValidationResults(
                                sheet.getSheetName(),
                                rule.getCell(),
                                rule.getFieldName(),
                                value,
                                rule.getFailMessage() != null ? rule.getFailMessage() : "Missing or invalid value for " + rule.getFieldName()
                        )
                );
            } else {
                passedFields.add(
                        new CellValidationResults(
                                sheet.getSheetName(),
                                rule.getCell(),
                                rule.getFieldName(),
                                value,
                                "Field has value"
                        )
                );
            }
        } else if(
                "REQUIRED_DATE".equalsIgnoreCase(rule.getValidationType())
        ){
            if(value.isBlank()){
                errors.add(
                        new CellValidationResults(
                                sheet.getSheetName(),
                                rule.getCell(),
                                rule.getFieldName(),
                                value,
                                rule.getFailMessage() != null ? rule.getFailMessage() : "Missing or invalid value for " + rule.getFieldName()
                        )
                );
                return;
            }
            boolean validDate = false;

            if(cell != null){
                if(
                        cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)
                ){
                    validDate = true;
                }
            }

            if(!validDate){
                errors.add(
                        new CellValidationResults(
                                sheet.getSheetName(),
                                rule.getCell(),
                                rule.getFieldName(),
                                value,
                                rule.getFailMessage() != null ? rule.getFailMessage() : "Missing or invalid value for " + rule.getFieldName()
                        )
                );
            } else {
                passedFields.add(
                        new CellValidationResults(
                                sheet.getSheetName(),
                                rule.getCell(),
                                rule.getFieldName(),
                                value,
                                "Field has valid date"
                        )
                );
            }
        }
    }

    private boolean isValidDate(String value){
        try {
            LocalDate.parse(
                    value,
                    DateTimeFormatter.ISO_LOCAL_TIME
            );

            return true;
        } catch (DateTimeParseException ex){
            return false;
        }
    }
}