package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class EndConditionConfig {

    private String type;

    private List<String> fields;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}