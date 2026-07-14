package com.example.excelvalidator.service.validator;

import com.example.excelvalidator.model.CellValidationError;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class EmployeeSheetValidator implements SheetValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public String sheetName() {
        return "Employees";
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

            String employeeId =
                    getValue(row, 0, dataFormatter);

            String name =
                    getValue(row, 1, dataFormatter);

            String email =
                    getValue(row, 2, dataFormatter);

            String ageValue =
                    getValue(row, 3, dataFormatter);

            String status =
                    getValue(row, 4, dataFormatter);

            // Employee ID

            if (employeeId.isBlank()) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "A",
                                "Employee ID",
                                employeeId,
                                "Employee ID is required"
                        )
                );
            }

            // Name

            if (name.isBlank()) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "B",
                                "Name",
                                name,
                                "Name is required"
                        )
                );
            }

            // Email

            if (email.isBlank()) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "C",
                                "Email",
                                email,
                                "Email is required"
                        )
                );

            } else if (
                    !EMAIL_PATTERN
                            .matcher(email)
                            .matches()
            ) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "C",
                                "Email",
                                email,
                                "Invalid email format"
                        )
                );
            }

            // Age

            if (ageValue.isBlank()) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "D",
                                "Age",
                                ageValue,
                                "Age is required"
                        )
                );

            } else {

                try {

                    int age =
                            Integer.parseInt(ageValue);

                    if (age < 18 || age > 65) {

                        errors.add(
                                new CellValidationError(
                                        sheet.getSheetName(),
                                        rowIndex + 1,
                                        "D",
                                        "Age",
                                        ageValue,
                                        "Age must be between 18 and 65"
                                )
                        );
                    }

                } catch (NumberFormatException ex) {

                    errors.add(
                            new CellValidationError(
                                    sheet.getSheetName(),
                                    rowIndex + 1,
                                    "D",
                                    "Age",
                                    ageValue,
                                    "Age must be numeric"
                            )
                    );
                }
            }

            // Status

            if (
                    !status.equalsIgnoreCase("Active")
                            && !status.equalsIgnoreCase("Inactive")
            ) {

                errors.add(
                        new CellValidationError(
                                sheet.getSheetName(),
                                rowIndex + 1,
                                "E",
                                "Status",
                                status,
                                "Status must be Active or Inactive"
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