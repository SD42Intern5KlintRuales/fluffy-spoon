package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class ValidationConfig {

    private List<FileRuleConfig> files;

    public List<FileRuleConfig> getFiles() {
        return files;
    }

    public void setFiles(List<FileRuleConfig> files) {
        this.files = files;
    }
}