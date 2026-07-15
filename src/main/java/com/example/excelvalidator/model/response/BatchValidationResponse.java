package com.example.excelvalidator.model.response;

import java.util.List;

public class BatchValidationResponse {

    private String overallStatus;

    private int filesChecked;

    private int passedFiles;

    private int failedFiles;

    private int totalChecks;
    private int passedChecks;
    private int failedChecks;

    private List<FileValidationResult> results;

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(
            String overallStatus
    ) {
        this.overallStatus = overallStatus;
    }

    public int getFilesChecked() {
        return filesChecked;
    }

    public void setFilesChecked(
            int filesChecked
    ) {
        this.filesChecked = filesChecked;
    }

    public int getPassedFiles() {
        return passedFiles;
    }

    public void setPassedFiles(
            int passedFiles
    ) {
        this.passedFiles = passedFiles;
    }

    public int getFailedFiles() {
        return failedFiles;
    }

    public void setFailedFiles(
            int failedFiles
    ) {
        this.failedFiles = failedFiles;
    }

    public List<FileValidationResult> getResults() {
        return results;
    }

    public void setResults(
            List<FileValidationResult> results
    ) {
        this.results = results;
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