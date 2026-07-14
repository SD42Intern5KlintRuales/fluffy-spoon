package com.example.excelvalidator.model;

public record CellValidationError(
        String sheet,
        int row,
        String column,
        String field,
        String value,
        String message
) {
}
