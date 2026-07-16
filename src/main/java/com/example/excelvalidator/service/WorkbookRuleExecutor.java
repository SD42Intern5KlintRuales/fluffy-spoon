package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkbookRuleExecutor implements RuleExecutor<FileRuleConfig> {

    private final WorkbookRuleEngine workbookRuleEngine;

    public WorkbookRuleExecutor(WorkbookRuleEngine workbookRuleEngine) {
        this.workbookRuleEngine = workbookRuleEngine;
    }

    @Override
    public String getKey() {
        return "requiredSheets";
    }

    @Override
    public void execute(Workbook workbook, FileRuleConfig ruleObject, List<CellValidationError> errors) {
        if (ruleObject == null) {
            return;
        }

        workbookRuleEngine.validateRequiredSheets(workbook, ruleObject, errors);
    }
}
