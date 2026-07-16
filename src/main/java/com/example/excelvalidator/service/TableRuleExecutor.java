package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.validation.v2.TableRuleConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableRuleExecutor implements RuleExecutor<List<TableRuleConfig>> {

    private static final Logger log = LoggerFactory.getLogger(TableRuleExecutor.class);

    private final TableRuleEngine tableRuleEngine;

    public TableRuleExecutor(TableRuleEngine tableRuleEngine) {
        this.tableRuleEngine = tableRuleEngine;
    }

    @Override
    public String getKey() {
        return "tableRules";
    }

    @Override
    public void execute(Workbook workbook, List<TableRuleConfig> ruleObject, List<CellValidationError> errors) {
        if (ruleObject == null) {
            return;
        }

        log.debug("Executing TableRuleExecutor for {} table rules", ruleObject.size());

        tableRuleEngine.validateTables(workbook, ruleObject, errors);
    }
}
