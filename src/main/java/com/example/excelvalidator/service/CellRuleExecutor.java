package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.validation.v2.CellRuleConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CellRuleExecutor implements RuleExecutor<List<CellRuleConfig>> {

    private final CellRuleEngine cellRuleEngine;

    public CellRuleExecutor(CellRuleEngine cellRuleEngine) {
        this.cellRuleEngine = cellRuleEngine;
    }

    @Override
    public String getKey() {
        return "cellRules";
    }

    @Override
    public void execute(Workbook workbook, List<CellRuleConfig> ruleObject, List<CellValidationError> errors) {
        if (ruleObject == null) {
            return;
        }

        cellRuleEngine.validateCellRules(workbook, ruleObject, errors);
    }
}
