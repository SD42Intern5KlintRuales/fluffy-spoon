package com.example.excelvalidator.model.validation;

import java.util.List;

public class SheetRuleConfig {

    private String name;

    private List<ColumnRuleConfig> columns;

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnRuleConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnRuleConfig> columns) {
        this.columns = columns;
    }
}
