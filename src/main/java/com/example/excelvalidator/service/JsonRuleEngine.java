package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.validation.ColumnRuleConfig;
import com.example.excelvalidator.model.validation.RuleConfig;
import com.example.excelvalidator.model.validation.SheetRuleConfig;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public class JsonRuleEngine {

    public int validateSheet(
            Sheet sheet,
            SheetRuleConfig sheetConfig,
            DataFormatter formatter,
            List<CellValidationError> errors
    ){
        int rowsChecked = 0;

        for(
                int rowIndex = 1;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++
        ){
            Row row = sheet.getRow(rowIndex);

            if (
                    row == null
                            || isRowEmpty(
                            row,
                            formatter
                    )
            ) {
                continue;
            }

            rowsChecked++;

            for(
                    ColumnRuleConfig columnConfig: sheetConfig.getColumns()
            ) {
                validateColumn(
                        sheet,
                        row,
                        rowIndex,
                        columnConfig,
                        formatter,
                        errors
                );
            }
        }
        return rowsChecked;
    }

    private void validateColumn(
            Sheet sheet,
            Row row,
            int rowIndex,
            ColumnRuleConfig columnConfig,
            DataFormatter formatter,
            List<CellValidationError> errors
    ){
        int columnIndex = columnToIndex(columnConfig.getColumn());

        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        String value = "";

        if(cell != null){
            value = formatter.formatCellValue(cell).trim();
        }

        for (RuleConfig rule : columnConfig.getRules()) {

            if (
                    "required".equalsIgnoreCase(
                            rule.getType()
                    )
            ) {

                if (value.isBlank()) {

                    errors.add(
                            new CellValidationError(
                                    sheet.getSheetName(),
                                    rowIndex + 1,
                                    columnConfig.getColumn(),
                                    columnConfig.getField(),
                                    value,
                                    columnConfig.getField()
                                            + " is required"
                            )
                    );
                }
            }
        }
    }

    private int columnToIndex(
            String column
    ){
        return  column.charAt(0) - 'A';
    }

    private boolean isRowEmpty(
            Row row,
            DataFormatter formatter
    ) {

        if (row == null) {
            return true;
        }

        for (Cell cell : row) {

            if (!formatter
                    .formatCellValue(cell)
                    .trim()
                    .isEmpty()) {

                return false;
            }
        }

        return true;
    }
}
