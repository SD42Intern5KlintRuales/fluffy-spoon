package com.example.excelvalidator.model.validation.v2;

import java.util.List;

public class ValidationConfig {

    private String configVersion;

    private String lastUpdated;

    private SettingsConfig settings;

    private List<FileRuleConfig> files;

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public SettingsConfig getSettings() {
        return settings;
    }

    public void setSettings(SettingsConfig settings) {
        this.settings = settings;
    }

    public List<FileRuleConfig> getFiles() {
        return files;
    }

    public void setFiles(List<FileRuleConfig> files) {
        this.files = files;
    }
}