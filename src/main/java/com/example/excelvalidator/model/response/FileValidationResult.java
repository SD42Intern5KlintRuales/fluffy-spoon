package com.example.excelvalidator.model.response;

import com.example.excelvalidator.model.CellValidationError;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "fileName",
        "fileType",
        "status",
        "valid",
        "message",
        "totalChecks",
        "passedChecks",
        "failedChecks",
        "sheetValidations",
        "fieldValidations"
})
public class FileValidationResult {

    private String fileType;

    private String fileName;

    private String status;

    private boolean valid;

    private int totalChecks;

    private int passedChecks;

    private int failedChecks;

    private String message;

    private SheetValidationSummary sheetValidations;

    private FieldValidationSummary fieldValidations;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(
            String fileType
    ) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(
            String fileName
    ) {
        this.fileName = fileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status
    ) {
        this.status = status;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(
            boolean valid
    ) {
        this.valid = valid;
    }

    public SheetValidationSummary getSheetValidations() {
        return sheetValidations;
    }

    public void setSheetValidations(SheetValidationSummary sheetValidations) {
        this.sheetValidations = sheetValidations;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FieldValidationSummary getFieldValidations() {
        return fieldValidations;
    }

    public void setFieldValidations(FieldValidationSummary fieldValidations) {
        this.fieldValidations = fieldValidations;
    }
    public int getTotalChecks() {
        return totalChecks;
    }

    public int getPassedChecks() {
        return passedChecks;
    }

    public int getFailedChecks() {
        return failedChecks;
    }

    public void setTotalChecks(int totalChecks) {
        this.totalChecks = totalChecks;
    }

    public void setPassedChecks(int passedChecks) {
        this.passedChecks = passedChecks;
    }

    public void setFailedChecks(int failedChecks) {
        this.failedChecks = failedChecks;
    }
}