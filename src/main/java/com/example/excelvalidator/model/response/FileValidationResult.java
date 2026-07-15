package com.example.excelvalidator.model.response;

import com.example.excelvalidator.model.CellValidationError;

import java.util.List;

public class FileValidationResult {

    private String fileType;

    private String fileName;

    private String status;

    private boolean valid;

    private int totalChecks;

    private int passedChecks;

    private int failedChecks;
    private int errorCount;

    private String message;

    private List<CellValidationError> errors;

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

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(
            int errorCount
    ) {
        this.errorCount = errorCount;
    }

    public List<CellValidationError> getErrors() {
        return errors;
    }

    public void setErrors(
            List<CellValidationError> errors
    ) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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