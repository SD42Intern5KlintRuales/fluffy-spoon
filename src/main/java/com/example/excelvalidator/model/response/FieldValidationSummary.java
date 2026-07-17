package com.example.excelvalidator.model.response;

import com.example.excelvalidator.model.CellValidationResults;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "passedFieldChecks",
        "failedFieldChecks",
        "passedFields",
        "failedFields"
})
public class FieldValidationSummary {
    private int passedFieldChecks;
    private int failedFieldChecks;
    private List<CellValidationResults> passedFields;
    private List<CellValidationResults> failedFields;

    public int getPassedFieldChecks() {
        return passedFieldChecks;
    }

    public void setPassedFieldChecks(int passedFieldChecks) {
        this.passedFieldChecks = passedFieldChecks;
    }

    public int getFailedFieldChecks() {
        return failedFieldChecks;
    }

    public void setFailedFieldChecks(int failedFieldChecks) {
        this.failedFieldChecks = failedFieldChecks;
    }

    public List<CellValidationResults> getPassedFields() {
        return passedFields;
    }

    public void setPassedFields(List<CellValidationResults> passedFields) {
        this.passedFields = passedFields;
    }

    public List<CellValidationResults> getFailedFields() {
        return failedFields;
    }

    public void setFailedFields(List<CellValidationResults> failedFields) {
        this.failedFields = failedFields;
    }
}
