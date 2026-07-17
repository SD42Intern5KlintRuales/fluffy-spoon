package com.example.excelvalidator.model;

import java.util.List;

public record ExcelValidationResponse(
        String fileName,
        boolean valid,
        int rowsChecked,
        int failedChecks,
        int passedChecks,
        List<String> sheets,
        List<CellValidationResults> passedFields,
        List<CellValidationResults> errors
) {
}
