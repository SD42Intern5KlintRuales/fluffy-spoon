package com.example.excelvalidator.model.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "sheetsChecked",
        "presentSheets",
        "missingSheets"
})
public class SheetValidationSummary {
    private int sheetsChecked;
    private List<String> presentSheets;
    private List<String> missingSheets;

    public int getSheetsChecked() {
        return sheetsChecked;
    }

    public void setSheetsChecked(int sheetsChecked) {
        this.sheetsChecked = sheetsChecked;
    }

    public List<String> getPresentSheets() {
        return presentSheets;
    }

    public void setPresentSheets(List<String> presentSheets) {
        this.presentSheets = presentSheets;
    }

    public List<String> getMissingSheets() {
        return missingSheets;
    }

    public void setMissingSheets(List<String> missingSheets) {
        this.missingSheets = missingSheets;
    }
}
