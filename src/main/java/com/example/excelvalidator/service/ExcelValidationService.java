package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import com.example.excelvalidator.model.validation.v2.ValidationConfig;
import tools.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelValidationService {

    private final ObjectMapper mapper =
            new ObjectMapper();

    private final WorkbookRuleEngine workbookRuleEngine;
    private final CellRuleEngine cellRuleEngine;
    private final TableRuleEngine tableRuleEngine;

    public ExcelValidationService(
            WorkbookRuleEngine workbookRuleEngine,
            CellRuleEngine cellRuleEngine,
            TableRuleEngine tableRuleEngine
    ) {
        this.workbookRuleEngine =
                workbookRuleEngine;
        this.cellRuleEngine = cellRuleEngine;
        this.tableRuleEngine = tableRuleEngine;
    }

    public ExcelValidationResponse validate(
            MultipartFile excelFile,
            MultipartFile rulesFile,
            String fileType
    ) {

        List<String> sheets =
                new ArrayList<>();

        List<CellValidationError> errors =
                new ArrayList<>();

        int rowsChecked = 0;

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                excelFile.getInputStream()
                        )
        ) {

            ValidationConfig config =
                    mapper.readValue(
                            rulesFile.getInputStream(),
                            ValidationConfig.class
                    );

            FileRuleConfig fileConfig =
                    findFileConfig(
                            config,
                            fileType
                    );

            if (fileConfig == null) {

                throw new RuntimeException(
                        "No configuration found for file type: "
                                + fileType
                );
            }

            for (
                    int sheetIndex = 0;
                    sheetIndex < workbook.getNumberOfSheets();
                    sheetIndex++
            ) {

                Sheet sheet =
                        workbook.getSheetAt(
                                sheetIndex
                        );

                sheets.add(
                        sheet.getSheetName()
                );
            }

            workbookRuleEngine
                    .validateRequiredSheets(
                            workbook,
                            fileConfig,
                            errors
                    );

            cellRuleEngine.validateCellRules(
                    workbook,
                    fileConfig.getCellRules(),
                    errors
            );

            tableRuleEngine.validateTables(
                    workbook,
                    fileConfig.getTableRules(),
                    errors
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error reading Excel file",
                    ex
            );
        }

        return new ExcelValidationResponse(
                excelFile.getOriginalFilename(),
                errors.isEmpty(),
                rowsChecked,
                errors.size(),
                sheets,
                errors
        );
    }

    private FileRuleConfig findFileConfig(
            ValidationConfig config,
            String fileType
    ) {

        return config.getFiles()
                .stream()
                .filter(
                        file ->
                                file.getFileType()
                                        .equalsIgnoreCase(
                                                fileType
                                        )
                )
                .findFirst()
                .orElse(null);
    }
}