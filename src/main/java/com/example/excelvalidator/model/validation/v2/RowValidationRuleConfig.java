package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class RowValidationRuleConfig {

    private String type;

    private String ifField;

    private String requiredField;

    private String field;

    private List<String> dataFields;

    private String message;

    public String getType() {
        return type;
    }

    public void setType(
            String type
    ) {
        this.type = type;
    }

    public String getIfField() {
        return ifField;
    }

    public void setIfField(
            String ifField
    ) {
        this.ifField = ifField;
    }

    public String getRequiredField() {
        return requiredField;
    }

    public void setRequiredField(
            String requiredField
    ) {
        this.requiredField = requiredField;
    }

    public String getField() {
        return field;
    }

    public void setField(
            String field
    ) {
        this.field = field;
    }

    public List<String> getDataFields() {
        return dataFields;
    }

    public void setDataFields(
            List<String> dataFields
    ) {
        this.dataFields = dataFields;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message = message;
    }
}