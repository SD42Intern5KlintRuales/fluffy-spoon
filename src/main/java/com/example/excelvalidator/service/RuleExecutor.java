package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface RuleExecutor<T> {

    String getKey();

    void execute(Workbook workbook, T ruleObject, List<CellValidationError> errors);

    default int estimateChecks(T ruleObject) {
        return 0;
    }
}
