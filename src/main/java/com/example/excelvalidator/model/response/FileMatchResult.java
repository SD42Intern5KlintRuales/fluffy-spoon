package com.example.excelvalidator.model.response;

import com.example.excelvalidator.model.validation.v2.FileRuleConfig;

import java.util.List;

public class FileMatchResult {

    private boolean matched;

    private FileRuleConfig fileConfig;

    private List<String> missingSheets;

    private int matchedSheetCount;

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public FileRuleConfig getFileConfig() {
        return fileConfig;
    }

    public void setFileConfig(
            FileRuleConfig fileConfig
    ) {
        this.fileConfig = fileConfig;
    }

    public List<String> getMissingSheets() {
        return missingSheets;
    }

    public void setMissingSheets(
            List<String> missingSheets
    ) {
        this.missingSheets = missingSheets;
    }

    public int getMatchedSheetCount() {
        return matchedSheetCount;
    }

    public void setMatchedSheetCount(int matchedSheetCount) {
        this.matchedSheetCount = matchedSheetCount;
    }
}