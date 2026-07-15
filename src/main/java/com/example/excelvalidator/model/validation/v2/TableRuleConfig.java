package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class TableRuleConfig {

    private String sheet;

    private String tableName;

    private Integer startRow;

    private EndConditionConfig endCondition;

    private List<TableColumnConfig> columns;

    private List<RowValidationRuleConfig> rowValidationRules;

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public EndConditionConfig getEndCondition() {
        return endCondition;
    }

    public void setEndCondition(EndConditionConfig endCondition) {
        this.endCondition = endCondition;
    }

    public List<TableColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(
            List<TableColumnConfig> columns
    ) {
        this.columns = columns;
    }

    public List<RowValidationRuleConfig> getRowValidationRules() {
        return rowValidationRules;
    }

    public void setRowValidationRules(
            List<RowValidationRuleConfig> rowValidationRules
    ) {
        this.rowValidationRules =
                rowValidationRules;
    }
}
