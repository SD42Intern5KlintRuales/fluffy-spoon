package com.example.excelvalidator.model.validation.v2;

public class CellRuleConfig {

    private String sheet;

    private String fieldName;

    private String cell;

    private String validationType;

    private String failMessage;

    private String note;

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(
            String validationType
    ) {
        this.validationType = validationType;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(
            String failMessage
    ) {
        this.failMessage = failMessage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
