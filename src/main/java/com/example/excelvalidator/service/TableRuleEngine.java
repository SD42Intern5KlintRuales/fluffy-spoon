package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.validation.v2.RowValidationRuleConfig;
import com.example.excelvalidator.model.validation.v2.TableColumnConfig;
import com.example.excelvalidator.model.validation.v2.TableRuleConfig;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TableRuleEngine {

    public void validateTables(
            Workbook workbook,
            List<TableRuleConfig> tableRules,
            List<CellValidationError> errors
    ) {

        if (tableRules == null) {
            return;
        }

        for (TableRuleConfig tableRule : tableRules) {

            validateTable(
                    workbook,
                    tableRule,
                    errors
            );
        }
    }

    private void validateTable(
            Workbook workbook,
            TableRuleConfig tableRule,
            List<CellValidationError> errors
    ) {

        Sheet sheet =
                workbook.getSheet(
                        tableRule.getSheet()
                );

        if (sheet == null) {
            return;
        }

        String currentCategory = "";

        for (
                int rowIndex =
                tableRule.getStartRow() - 1;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++
        ) {

            Row row =
                    sheet.getRow(rowIndex);

            if (row == null) {
                break;
            }

            Map<String, String> values =
                    new HashMap<>();

            for (
                    TableColumnConfig columnConfig
                    : tableRule.getColumns()
            ) {

                int columnIndex =
                        columnToIndex(
                                columnConfig.getColumn()
                        );

                String value =
                        getCellValue(
                                row,
                                columnIndex
                        );

                values.put(
                        columnConfig.getFieldName(),
                        value
                );

                if (
                        Boolean.TRUE.equals(
                                columnConfig.getRequired()
                        )
                                && value.isBlank()
                ) {

                    errors.add(
                            new CellValidationError(
                                    sheet.getSheetName(),
                                    rowIndex + 1,
                                    columnConfig.getColumn(),
                                    columnConfig.getFieldName(),
                                    value,
                                    columnConfig.getFailMessage()
                            )
                    );
                }
            }

            if (
                    shouldStopProcessing(
                            tableRule,
                            values
                    )
            ) {
                break;
            }

            currentCategory =
                    applyRowValidationRules(
                            tableRule,
                            values,
                            currentCategory,
                            sheet.getSheetName(),
                            rowIndex,
                            errors
                    );
        }
    }

    private String applyRowValidationRules(
            TableRuleConfig tableRule,
            Map<String, String> values,
            String currentCategory,
            String sheetName,
            int rowIndex,
            List<CellValidationError> errors
    ) {

        if (tableRule.getRowValidationRules() == null) {
            return currentCategory;
        }

        for (
                RowValidationRuleConfig rule
                : tableRule.getRowValidationRules()
        ) {

            // DEPENDENCY

            if (
                    "DEPENDENCY".equalsIgnoreCase(
                            rule.getType()
                    )
            ) {

                String triggerValue =
                        getFieldValue(
                                values,
                                rule.getIfField()
                        );

                String requiredValue =
                        getFieldValue(
                                values,
                                rule.getRequiredField()
                        );

                if (
                        !triggerValue.isBlank()
                                && requiredValue.isBlank()
                ) {

                    errors.add(
                            new CellValidationError(
                                    sheetName,
                                    rowIndex + 1,
                                    findColumn(
                                            tableRule,
                                            rule.getRequiredField()
                                    ),
                                    rule.getRequiredField(),
                                    requiredValue,
                                    rule.getMessage()
                            )
                    );
                }
            }

            // GROUP_HEADER_REQUIRED

            else if (
                    "GROUP_HEADER_REQUIRED"
                            .equalsIgnoreCase(
                                    rule.getType()
                            )
            ) {

                String category =
                        getFieldValue(
                                values,
                                rule.getField()
                        );

                boolean rowHasData = false;

                if (rule.getDataFields() != null) {

                    for (
                            String dataField
                            : rule.getDataFields()
                    ) {

                        String value =
                                getFieldValue(
                                        values,
                                        dataField
                                );

                        if (!value.isBlank()) {

                            rowHasData = true;
                            break;
                        }
                    }
                }

                // new category starts a group

                if (!category.isBlank()) {

                    currentCategory =
                            category;
                }

                // first populated row must have category

                if (
                        rowHasData
                                && category.isBlank()
                                && currentCategory.isBlank()
                ) {

                    errors.add(
                            new CellValidationError(
                                    sheetName,
                                    rowIndex + 1,
                                    findColumn(
                                            tableRule,
                                            rule.getField()
                                    ),
                                    rule.getField(),
                                    category,
                                    rule.getMessage()
                            )
                    );
                }
            }
        }

        return currentCategory;
    }

    private boolean shouldStopProcessing(
            TableRuleConfig tableRule,
            Map<String, String> values
    ) {

        if (
                tableRule.getEndCondition()
                        == null
        ) {
            return false;
        }

        if (
                !"ALL_FIELDS_BLANK".equalsIgnoreCase(
                        tableRule.getEndCondition()
                                .getType()
                )
        ) {
            return false;
        }

        for (
                String field :
                tableRule.getEndCondition()
                        .getFields()
        ) {

            String value =
                    getFieldValue(
                            values,
                            field
                    );

            if (!value.isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String getFieldValue(
            Map<String, String> values,
            String fieldName
    ) {

        return values.getOrDefault(
                fieldName,
                ""
        );
    }

    private String findColumn(
            TableRuleConfig tableRule,
            String fieldName
    ) {

        return tableRule.getColumns()
                .stream()
                .filter(
                        column ->
                                fieldName.equalsIgnoreCase(
                                        column.getFieldName()
                                )
                )
                .map(
                        TableColumnConfig::getColumn
                )
                .findFirst()
                .orElse("");
    }

    private int columnToIndex(
            String column
    ) {

        return column
                .toUpperCase()
                .charAt(0) - 'A';
    }

    private String getCellValue(
            Row row,
            int column
    ) {

        DataFormatter formatter =
                new DataFormatter();

        Cell cell =
                row.getCell(
                        column,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
                );

        if (cell == null) {
            return "";
        }

        return formatter
                .formatCellValue(cell)
                .trim();
    }
}