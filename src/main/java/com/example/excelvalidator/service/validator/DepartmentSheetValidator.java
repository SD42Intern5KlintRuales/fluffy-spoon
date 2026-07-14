package com.example.excelvalidator.service.validator;

import com.example.excelvalidator.model.CellValidationError;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartmentSheetValidator implements SheetValidator {

    @Override
    public String sheetName() {
        return "Departments";
    }

    @Override
    public int validate(
            Sheet sheet,
            DataFormatter dataFormatter,
            List<CellValidationError> errors
    ) {

        int rowsChecked = 0;

        if(sheet.getLastRowNum() == 0){
            errors.add(
                    new CellValidationError(
                            sheet.getSheetName(),
                            1,
                            "",
                            "Sheet",
                            "",
                            "Sheet contains headers but no data rows"
                    )
            );

            return 0;
        }

        for (
                int rowIndex = 1;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++
        ) {

            Row row = sheet.getRow(rowIndex);

            if (row == null) {
                continue;
            }

            rowsChecked++;

            String departmentId =
                    getValue(row, 0, dataFormatter);

            String departmentName =
                    getValue(row, 1, dataFormatter);

            if (departmentId.isBlank()) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "A",
                                "Department ID",
                                departmentId,
                                "Department ID is required"
                        )
                );
            }

            // Name

            if (departmentName.isBlank()) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "B",
                                "Department Name",
                                departmentName,
                                "Department name is required"
                        )
                );
            }
        }

        return rowsChecked;
    }

    private String getValue(
            Row row,
            int column,
            DataFormatter dataFormatter
    ) {

        Cell cell =
                row.getCell(
                        column,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                );

        if (cell == null) {
            return "";
        }

        return dataFormatter
                .formatCellValue(cell)
                .trim();
    }
}