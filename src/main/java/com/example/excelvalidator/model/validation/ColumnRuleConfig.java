package com.example.excelvalidator.model.validation;

import java.util.List;

public class ColumnRuleConfig {

    private String column;

    private String field;

    private List<RuleConfig> rules;

    public String getColumn() {
        return column;
    }

    public String getField() {
        return field;
    }

    public List<RuleConfig> getRules() {
        return rules;
    }

    public void setRules(List<RuleConfig> rules) {
        this.rules = rules;
    }
}
