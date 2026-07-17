package com.example.excelvalidator.model;

public record CellValidationResults(
        String sheet,
        String cell,
        String field,
        String value,
        String message
) {
}
