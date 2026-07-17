package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationResults;
import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.model.response.BatchValidationResponse;
import com.example.excelvalidator.model.response.FileMatchResult;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import com.example.excelvalidator.model.validation.v2.ValidationConfig;
import com.example.excelvalidator.model.validation.v2.TableRuleConfig;
import com.example.excelvalidator.model.validation.v2.CellRuleConfig;
import com.example.excelvalidator.model.response.FileValidationResult;
import com.example.excelvalidator.service.engine.CellRuleEngine;
import com.example.excelvalidator.service.engine.TableRuleEngine;
import com.example.excelvalidator.service.engine.WorkbookRuleEngine;
import com.example.excelvalidator.model.response.SheetValidationSummary;
import com.example.excelvalidator.model.response.FieldValidationSummary;
import tools.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

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
                                    workbook
                            );

                    List<String> workbookSheets = new ArrayList<>();
                    for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                        workbookSheets.add(
                                workbook.getSheetAt(sheetIndex).getSheetName()
                        );
                    }

                    FileValidationResult result;

                    if (!matchResult.isMatched()) {

                        result =
                                buildMissingSheetResult(
                                        file.getOriginalFilename(),
                                        matchResult.getMissingSheets(),
                                        workbookSheets,
                                        matchResult.getMatchedSheetCount() > 0
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
            Workbook workbook
    ) {

        FileMatchResult result =
                new FileMatchResult();

        List<String> closestMissingSheets =
                new ArrayList<>();

        int bestMatchedCount =
                -1;

        int smallestMissingCount =
                Integer.MAX_VALUE;

        for (
                FileRuleConfig fileConfig
                : config.getFiles()
        ) {

            if (
                    fileConfig.getRequiredSheets()
                            == null
            ) {
                continue;
            }

            List<String> missingSheets =
                    new ArrayList<>();

            for (
                    String requiredSheet
                    : fileConfig.getRequiredSheets()
            ) {

                if (
                        workbook.getSheet(
                                requiredSheet
                        ) == null
                ) {
                    missingSheets.add(
                            requiredSheet
                    );
                }
            }

            int matchedCount =
                    fileConfig.getRequiredSheets().size()
                            - missingSheets.size();

            if (missingSheets.isEmpty()) {

                result.setMatched(
                        true
                );

                result.setFileConfig(
                        fileConfig
                );

                result.setMissingSheets(
                        new ArrayList<>()
                );

                return result;
            }

            if (
                    matchedCount > bestMatchedCount
                            || (matchedCount == bestMatchedCount
                                    && missingSheets.size() < smallestMissingCount)
            ) {

                bestMatchedCount =
                        matchedCount;

                smallestMissingCount =
                        missingSheets.size();

                closestMissingSheets =
                        missingSheets;

                result.setFileConfig(
                        fileConfig
                );

                result.setMatchedSheetCount(
                        matchedCount
                );
            }
        }

        result.setMatched(
                false
        );

        result.setMissingSheets(
                closestMissingSheets
        );

        return result;
    }

    private FileValidationResult buildMissingSheetResult(
            String fileName,
            List<String> missingSheets,
            List<String> presentSheets,
            boolean candidateHasMatch
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
        sheetValidations.setSheetsChecked(presentSheets.size());
        sheetValidations.setPresentSheets(presentSheets);
        sheetValidations.setMissingSheets(missingSheets);

        FieldValidationSummary fieldValidations = new FieldValidationSummary();
        fieldValidations.setPassedFieldChecks(0);
        fieldValidations.setFailedFieldChecks(0);
        fieldValidations.setPassedFields(new ArrayList<>());
        fieldValidations.setFailedFields(new ArrayList<>());

        result.setSheetValidations(sheetValidations);
        result.setFieldValidations(fieldValidations);

        if (candidateHasMatch) {
            result.setMessage(
                    "Workbook does not match any configured validation template. "
                            + "Found sheets: "
                            + (presentSheets.isEmpty() ? "none" : String.join(", ", presentSheets))
                            + ". Missing required sheet(s): "
                            + (missingSheets.isEmpty() ? "none" : String.join(", ", missingSheets))
                            + ". Please verify the workbook sheet names or update the validation rules."
            );
        } else {
            result.setMessage(
                    "Workbook does not match any configured validation template. "
                            + "The workbook sheets do not match any known file type configuration. "
                            + "Please verify the workbook contains an expected sheet set or add a new rule template."
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
                fileName
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