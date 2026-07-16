package com.example.excelvalidator.model.response;

import com.example.excelvalidator.model.CellValidationError;
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
    private List<CellValidationError> passedFields;
    private List<CellValidationError> failedFields;

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

    public List<CellValidationError> getPassedFields() {
        return passedFields;
    }

    public void setPassedFields(List<CellValidationError> passedFields) {
        this.passedFields = passedFields;
    }

    public List<CellValidationError> getFailedFields() {
        return failedFields;
    }

    public void setFailedFields(List<CellValidationError> failedFields) {
        this.failedFields = failedFields;
    }
}
