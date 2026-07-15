package com.example.excelvalidator.model.validation;

import java.util.List;

public class ValidationConfig {

    private List<SheetRuleConfig> sheets;

    public List<SheetRuleConfig> getSheets(){
        return sheets;
    }

    public void setSheets(
            List<SheetRuleConfig> sheets
    ){
        this.sheets = sheets;
    }
}
