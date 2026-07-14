package com.example.excelvalidator.service.validator;

import com.example.excelvalidator.model.CellValidationError;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

public interface SheetValidator {

    String sheetName();

    int validate(
            Sheet sheet,
            DataFormatter formatter,
            List<CellValidationError> errors
    );
}