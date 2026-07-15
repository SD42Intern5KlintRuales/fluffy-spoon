package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class FileRuleConfig {

    private String fileType;

    private String displayName;

    private String status;

    private String statusNote;

    private List<String> requiredSheets;

    private List<CellRuleConfig> cellRules;

    private List<TableRuleConfig> tableRules;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusNote() {
        return statusNote;
    }

    public void setStatusNote(String statusNote) {
        this.statusNote = statusNote;
    }

    public List<String> getRequiredSheets() {
        return requiredSheets;
    }

    public void setRequiredSheets(
            List<String> requiredSheets
    ) {
        this.requiredSheets = requiredSheets;
    }

    public List<CellRuleConfig> getCellRules() {
        return cellRules;
    }

    public void setCellRules(
            List<CellRuleConfig> cellRules
    ) {
        this.cellRules = cellRules;
    }

    public List<TableRuleConfig> getTableRules() {
        return tableRules;
    }

    public void setTableRules(
            List<TableRuleConfig> tableRules
    ) {
        this.tableRules = tableRules;
    }
}