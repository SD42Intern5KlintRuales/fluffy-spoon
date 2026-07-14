package com.example.excelvalidator.model;

import java.util.List;

public record ExcelUploadResponse(
        String fileName,
        long fileSize,
        int numberOfSheets,
        List<String> sheetNames,
        String message
) {
}
