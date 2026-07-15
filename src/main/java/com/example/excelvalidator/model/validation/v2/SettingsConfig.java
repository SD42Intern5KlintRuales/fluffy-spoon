package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class SettingsConfig {

    private Boolean trimWhitespace;

    private Boolean treatWhitespaceOnlyAsBlank;

    private Boolean acceptPlaceholderValuesAsValid;

    private List<String> placeholderExamples;

    private String notes;

    public Boolean getTrimWhitespace() {
        return trimWhitespace;
    }

    public void setTrimWhitespace(Boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    public Boolean getTreatWhitespaceOnlyAsBlank() {
        return treatWhitespaceOnlyAsBlank;
    }

    public void setTreatWhitespaceOnlyAsBlank(
            Boolean treatWhitespaceOnlyAsBlank
    ) {
        this.treatWhitespaceOnlyAsBlank =
                treatWhitespaceOnlyAsBlank;
    }

    public Boolean getAcceptPlaceholderValuesAsValid() {
        return acceptPlaceholderValuesAsValid;
    }

    public void setAcceptPlaceholderValuesAsValid(
            Boolean acceptPlaceholderValuesAsValid
    ) {
        this.acceptPlaceholderValuesAsValid =
                acceptPlaceholderValuesAsValid;
    }

    public List<String> getPlaceholderExamples() {
        return placeholderExamples;
    }

    public void setPlaceholderExamples(
            List<String> placeholderExamples
    ) {
        this.placeholderExamples =
                placeholderExamples;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}