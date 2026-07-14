package com.example.excelvalidator.controller;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().body(
                Map.of("message", exception.getMessage())
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleFileTooLarge(
            MaxUploadSizeExceededException exception
    ) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                Map.of("message", "The uploaded file must not exceed 10 MB.")
        );
    }

    @ExceptionHandler(EncryptedDocumentException.class)
    public ResponseEntity<Map<String, String>> handleEncryptedFile(
            EncryptedDocumentException exception
    ) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "Password-protected Excel files are not supported."
                )
        );
    }

    @ExceptionHandler(NotOfficeXmlFileException.class)
    public ResponseEntity<Map<String, String>> handleInvalidExcelFile(
            NotOfficeXmlFileException exception
    ) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "The uploaded file is not a valid Excel workbook."
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedError(
            Exception exception
    ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "message",
                                "The Excel file could not be processed."
                        )
                );
    }
}