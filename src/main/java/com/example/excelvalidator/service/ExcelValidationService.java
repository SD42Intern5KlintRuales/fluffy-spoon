package com.example.excelvalidator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.excelvalidator.model.CellValidationResults;
import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.model.response.BatchValidationResponse;
import com.example.excelvalidator.model.response.FieldValidationSummary;
import com.example.excelvalidator.model.response.FileMatchResult;
import com.example.excelvalidator.model.response.FileValidationResult;
import com.example.excelvalidator.model.response.SheetValidationSummary;
import com.example.excelvalidator.model.validation.v2.CellRuleConfig;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import com.example.excelvalidator.model.validation.v2.TableRuleConfig;
import com.example.excelvalidator.model.validation.v2.ValidationConfig;
import com.example.excelvalidator.service.engine.CellRuleEngine;
import com.example.excelvalidator.service.engine.TableRuleEngine;
import com.example.excelvalidator.service.engine.WorkbookRuleEngine;

import tools.jackson.databind.ObjectMapper;

@Service
public class ExcelValidationService {

        private static final Logger log = LoggerFactory.getLogger(ExcelValidationService.class);

    private final ObjectMapper mapper =
            new ObjectMapper();

    private final WorkbookRuleEngine workbookRuleEngine;
    private final CellRuleEngine cellRuleEngine;
    private final TableRuleEngine tableRuleEngine;
    private final RuleExecutorRegistry ruleExecutorRegistry;

    public ExcelValidationService(
            WorkbookRuleEngine workbookRuleEngine,
            CellRuleEngine cellRuleEngine,
            TableRuleEngine tableRuleEngine,
            RuleExecutorRegistry ruleExecutorRegistry
    ) {
        this.workbookRuleEngine = workbookRuleEngine;
        this.cellRuleEngine = cellRuleEngine;
        this.tableRuleEngine = tableRuleEngine;
        this.ruleExecutorRegistry = ruleExecutorRegistry;
    }

    public BatchValidationResponse validateBatch(
            List<MultipartFile> files,
            MultipartFile rulesFile
    ) {

        try {

            ValidationConfig config =
                    mapper.readValue(
                            rulesFile.getInputStream(),
                            ValidationConfig.class
                    );

            if (config == null || config.getFiles() == null || config.getFiles().isEmpty()) {
                return buildConfigErrorResponse(
                        files,
                        "Validation configuration error: the rules JSON is invalid or incomplete. Please provide a valid 'files' array and ensure required rule fields are not null."
                );
            }

            List<FileValidationResult> results =
                    new ArrayList<>();

            for (
                    MultipartFile file
                    : files
            ) {

                try (
                        Workbook workbook =
                                WorkbookFactory.create(
                                        file.getInputStream()
                                )
                ) {

                    FileMatchResult matchResult =
                            findMatchingConfig(
                                    config,
                                    file.getOriginalFilename()
                            );

                    FileValidationResult result;

                    if (!matchResult.isMatched()) {

                        result =
                                buildNoMatchResult(
                                        file.getOriginalFilename(),
                                        config
                                );

                    } else {

                        result =
                                validateWorkbook(
                                        workbook,
                                        matchResult.getFileConfig(),
                                        file.getOriginalFilename()
                                );
                    }

                    results.add(
                            result
                    );

                } catch (Exception ex) {

                    results.add(
                            buildErrorResult(
                                    file.getOriginalFilename(),
                                    ex
                            )
                    );
                }
            }

            int passedFiles = 0;
            int failedFiles = 0;

            int totalChecks = 0;
            int passedChecks = 0;
            int failedChecks = 0;

            for (
                    FileValidationResult result
                    : results
            ) {

                if (result.isValid()) {

                    passedFiles++;

                } else {

                    failedFiles++;
                }

                totalChecks +=
                        result.getTotalChecks();

                passedChecks +=
                        result.getPassedChecks();

                failedChecks +=
                        result.getFailedChecks();
            }

            BatchValidationResponse response =
                    new BatchValidationResponse();

            response.setFilesChecked(
                    files.size()
            );

            response.setPassedFiles(
                    passedFiles
            );

            response.setFailedFiles(
                    failedFiles
            );

            response.setTotalChecks(
                    totalChecks
            );

            response.setPassedChecks(
                    passedChecks
            );

            response.setFailedChecks(
                    failedChecks
            );

            response.setOverallStatus(
                    failedFiles > 0
                            ? "FAILED"
                            : "PASSED"
            );

            response.setResults(
                    results
            );

            return response;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Unable to load validation rules",
                    ex
            );
        }
    }

    public ExcelValidationResponse validate(
            MultipartFile excelFile,
            MultipartFile rulesFile,
            String fileType
    ) {

        List<String> sheets =
                new ArrayList<>();

        List<CellValidationResults> errors =
                new ArrayList<>();

        List<CellValidationResults> passedFields =
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

            // Execute configured rule sections via registry-driven executors
            ruleExecutorRegistry
                    .get("requiredSheets")
                    .ifPresent(exec -> ((RuleExecutor<FileRuleConfig>) exec)
                            .execute(workbook, fileConfig, errors, passedFields));

            ruleExecutorRegistry
                    .get("cellRules")
                    .ifPresent(exec -> ((RuleExecutor<List<CellRuleConfig>>) exec)
                            .execute(workbook, fileConfig.getCellRules(), errors, passedFields));

            ruleExecutorRegistry
                    .get("tableRules")
                    .ifPresent(exec -> ((RuleExecutor<List<TableRuleConfig>>) exec)
                            .execute(workbook, fileConfig.getTableRules(), errors, passedFields));

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
                passedFields.size(),
                sheets,
                passedFields,
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
                                file.getFileType() != null
                                        && fileType != null
                                        && file.getFileType()
                                        .equalsIgnoreCase(
                                                fileType
                                        )
                )
                .findFirst()
                .orElse(null);
    }

    private FileMatchResult findMatchingConfig(
            ValidationConfig config,
            String uploadedFileName
    ) {

        FileMatchResult result =
                new FileMatchResult();

        String uploadedBaseName =
                stripExcelExtension(uploadedFileName);

        for (
                FileRuleConfig fileConfig
                : config.getFiles()
        ) {

            if (fileConfig.getFileType() == null) {
                continue;
            }

            String configBaseName =
                    stripExcelExtension(
                            fileConfig.getFileType()
                    );

            if (
                    configBaseName.equalsIgnoreCase(
                            uploadedBaseName
                    )
            ) {

                result.setMatched(true);

                result.setFileConfig(
                        fileConfig
                );

                result.setMissingSheets(
                        new ArrayList<>()
                );

                return result;
            }
        }

        result.setMatched(false);

        result.setMissingSheets(
                new ArrayList<>()
        );

        return result;
    }

    private String stripExcelExtension(String name) {
        if (name == null) {
            return "";
        }
        return name
                .replaceAll("(?i)\\.(xlsx|xls)$", "")
                .trim();
    }

    private FileValidationResult buildNoMatchResult(
            String fileName,
            ValidationConfig config
    ) {

        FileValidationResult result =
                new FileValidationResult();

        result.setFileName(
                fileName
        );

        result.setStatus(
                "FAILED"
        );

        result.setValid(
                false
        );

        result.setTotalChecks(
                1
        );

        result.setPassedChecks(
                0
        );

        result.setFailedChecks(
                1
        );

        SheetValidationSummary sheetValidations = new SheetValidationSummary();
        sheetValidations.setSheetsChecked(0);
        sheetValidations.setPresentSheets(new ArrayList<>());
        sheetValidations.setMissingSheets(new ArrayList<>());

        FieldValidationSummary fieldValidations = new FieldValidationSummary();
        fieldValidations.setPassedFieldChecks(0);
        fieldValidations.setFailedFieldChecks(0);
        fieldValidations.setPassedFields(new ArrayList<>());
        fieldValidations.setFailedFields(new ArrayList<>());

        result.setSheetValidations(sheetValidations);
        result.setFieldValidations(fieldValidations);

        List<String> availableTemplates = config.getFiles()
                .stream()
                .map(FileRuleConfig::getFileType)
                .filter(ft -> ft != null && !ft.isBlank())
                .collect(Collectors.toList());

        if (availableTemplates.isEmpty()) {
            result.setMessage(
                    "No validation templates are configured. "
                            + "Please provide a valid rules JSON file with configured file types."
            );
        } else {
            result.setMessage(
                    "No validation template found for file '" + fileName + "'. "
                            + "The filename does not match any configured file type. "
                            + "Available templates: "
                            + String.join(", ", availableTemplates)
                            + ". Please rename the file to match a template or add a new configuration."
            );
        }

        return result;
    }

    private FileValidationResult validateWorkbook(
            Workbook workbook,
            FileRuleConfig fileConfig,
            String fileName
    ) {

        FileValidationResult result =
                new FileValidationResult();

        List<CellValidationResults> errors =
                new ArrayList<>();

        List<CellValidationResults> passedFields =
                new ArrayList<>();

        // Use registry-driven executors for consistency with configuration-driven flow
        ruleExecutorRegistry
                .get("requiredSheets")
                .ifPresent(exec -> ((RuleExecutor<FileRuleConfig>) exec)
                        .execute(workbook, fileConfig, errors, passedFields));

        ruleExecutorRegistry
                .get("cellRules")
                .ifPresent(exec -> ((RuleExecutor<List<CellRuleConfig>>) exec)
                        .execute(workbook, fileConfig.getCellRules(), errors, passedFields));

        if (fileConfig.getTableRules() == null) {
            log.debug("No tableRules configured for fileType={}", fileConfig.getFileType());
        } else {
            log.debug("Found {} tableRules for fileType={}", fileConfig.getTableRules().size(), fileConfig.getFileType());
        }

        ruleExecutorRegistry
                .get("tableRules")
                .ifPresent(exec -> ((RuleExecutor<List<TableRuleConfig>>) exec)
                        .execute(workbook, fileConfig.getTableRules(), errors, passedFields));

        int failedChecks =
                errors.size();

        int passedChecks =
                passedFields.size();

        int totalChecks =
                failedChecks + passedChecks;

        result.setFileName(
                stripExcelExtension(fileName)
        );

        result.setFileType(
                fileConfig.getFileType()
        );

        // Separate sheet validations and field validations
        java.util.List<String> presentSheets = passedFields.stream()
                .filter(e -> "Workbook".equals(e.sheet()))
                .map(CellValidationResults::field)
                .collect(java.util.stream.Collectors.toList());
        
        java.util.List<String> missingSheets = errors.stream()
                .filter(e -> "Workbook".equals(e.sheet()))
                .map(CellValidationResults::field)
                .collect(java.util.stream.Collectors.toList());

        SheetValidationSummary sheetValidations = new SheetValidationSummary();
        sheetValidations.setSheetsChecked(fileConfig.getRequiredSheets() != null ? fileConfig.getRequiredSheets().size() : 0);
        sheetValidations.setPresentSheets(presentSheets);
        sheetValidations.setMissingSheets(missingSheets);

        java.util.List<CellValidationResults> fieldPassed = passedFields.stream()
                .filter(e -> !"Workbook".equals(e.sheet()))
                .collect(java.util.stream.Collectors.toList());
                
        java.util.List<CellValidationResults> fieldFailed = errors.stream()
                .filter(e -> !"Workbook".equals(e.sheet()))
                .collect(java.util.stream.Collectors.toList());

        FieldValidationSummary fieldValidations = new FieldValidationSummary();
        fieldValidations.setPassedFieldChecks(fieldPassed.size());
        fieldValidations.setFailedFieldChecks(fieldFailed.size());
        fieldValidations.setPassedFields(fieldPassed);
        fieldValidations.setFailedFields(fieldFailed);

        result.setSheetValidations(sheetValidations);
        result.setFieldValidations(fieldValidations);

        result.setTotalChecks(
                totalChecks
        );

        result.setPassedChecks(
                passedChecks
        );

        result.setFailedChecks(
                failedChecks
        );

        result.setValid(
                errors.isEmpty()
        );

        result.setStatus(
                errors.isEmpty()
                        ? "PASSED"
                        : "FAILED"
        );

        result.setMessage(
                errors.isEmpty()
                        ? "Validation passed"
                        : "Validation failed"
        );

        return result;
    }

    private BatchValidationResponse buildConfigErrorResponse(
            List<MultipartFile> files,
            String message
    ) {
        BatchValidationResponse response = new BatchValidationResponse();
        List<FileValidationResult> results = new ArrayList<>();

        for (MultipartFile file : files) {
            FileValidationResult result = new FileValidationResult();
            result.setFileName(file.getOriginalFilename());
            result.setStatus("ERROR");
            result.setValid(false);
            result.setTotalChecks(1);
            result.setPassedChecks(0);
            result.setFailedChecks(1);
            SheetValidationSummary sheetValidations = new SheetValidationSummary();
            sheetValidations.setSheetsChecked(0);
            sheetValidations.setPresentSheets(new ArrayList<>());
            sheetValidations.setMissingSheets(new ArrayList<>());

            FieldValidationSummary fieldValidations = new FieldValidationSummary();
            fieldValidations.setPassedFieldChecks(0);
            fieldValidations.setFailedFieldChecks(0);
            fieldValidations.setPassedFields(new ArrayList<>());
            fieldValidations.setFailedFields(new ArrayList<>());

            result.setSheetValidations(sheetValidations);
            result.setFieldValidations(fieldValidations);
            result.setMessage(message);
            results.add(result);
        }

        response.setFilesChecked(files.size());
        response.setPassedFiles(0);
        response.setFailedFiles(files.size());
        response.setTotalChecks(files.size());
        response.setPassedChecks(0);
        response.setFailedChecks(files.size());
        response.setOverallStatus("FAILED");
        response.setResults(results);

        return response;
    }

    private FileValidationResult buildErrorResult(
            String fileName,
            Exception ex
    ) {

        FileValidationResult result =
                new FileValidationResult();

        result.setFileName(
                fileName
        );

        result.setStatus(
                "ERROR"
        );

        result.setValid(
                false
        );

        result.setTotalChecks(
                1
        );

        result.setPassedChecks(
                0
        );

        result.setFailedChecks(
                1
        );

        SheetValidationSummary sheetValidations = new SheetValidationSummary();
        sheetValidations.setSheetsChecked(0);
        sheetValidations.setPresentSheets(new ArrayList<>());
        sheetValidations.setMissingSheets(new ArrayList<>());

        FieldValidationSummary fieldValidations = new FieldValidationSummary();
        fieldValidations.setPassedFieldChecks(0);
        fieldValidations.setFailedFieldChecks(0);
        fieldValidations.setPassedFields(new ArrayList<>());
        fieldValidations.setFailedFields(new ArrayList<>());

        result.setSheetValidations(sheetValidations);
        result.setFieldValidations(fieldValidations);

        result.setMessage(
                ex.getMessage() != null
                        ? (ex.getMessage().contains("The supplied file was empty")
                                ? "The uploaded file is empty. Please upload a valid Excel file."
                                : "Validation configuration error: invalid rule data provided. Please fix the rules JSON file and try again.")
                        : "Unexpected validation error"
        );

        return result;
    }
}