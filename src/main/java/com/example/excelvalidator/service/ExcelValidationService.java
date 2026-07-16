package com.example.excelvalidator.service;

import com.example.excelvalidator.model.CellValidationError;
import com.example.excelvalidator.model.ExcelValidationResponse;
import com.example.excelvalidator.model.response.BatchValidationResponse;
import com.example.excelvalidator.model.response.FileMatchResult;
import com.example.excelvalidator.model.validation.v2.FileRuleConfig;
import com.example.excelvalidator.model.validation.v2.ValidationConfig;
import com.example.excelvalidator.model.response.FileValidationResult;
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

                    FileValidationResult result;

                    if (!matchResult.isMatched()) {

                        result =
                                buildMissingSheetResult(
                                        file.getOriginalFilename(),
                                        matchResult.getMissingSheets()
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

            // Execute configured rule sections via registry-driven executors
            ruleExecutorRegistry
                    .get("requiredSheets")
                    .ifPresent(exec -> ((RuleExecutor<FileRuleConfig>) exec)
                            .execute(workbook, fileConfig, errors));

            ruleExecutorRegistry
                    .get("cellRules")
                    .ifPresent(exec -> ((RuleExecutor<List<com.example.excelvalidator.model.validation.v2.CellRuleConfig>>) exec)
                            .execute(workbook, fileConfig.getCellRules(), errors));

            ruleExecutorRegistry
                    .get("tableRules")
                    .ifPresent(exec -> ((RuleExecutor<List<com.example.excelvalidator.model.validation.v2.TableRuleConfig>>) exec)
                            .execute(workbook, fileConfig.getTableRules(), errors));

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
                    missingSheets.size()
                            < smallestMissingCount
            ) {

                smallestMissingCount =
                        missingSheets.size();

                closestMissingSheets =
                        missingSheets;
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

    private FileValidationResult validateWorkbook(
            Workbook workbook,
            FileRuleConfig fileConfig,
            String fileName
    ) {

        FileValidationResult result =
                new FileValidationResult();

        List<CellValidationError> errors =
                new ArrayList<>();

        // Use registry-driven executors for consistency with configuration-driven flow
        ruleExecutorRegistry
                .get("requiredSheets")
                .ifPresent(exec -> ((RuleExecutor<FileRuleConfig>) exec)
                        .execute(workbook, fileConfig, errors));

        ruleExecutorRegistry
                .get("cellRules")
                .ifPresent(exec -> ((RuleExecutor<List<com.example.excelvalidator.model.validation.v2.CellRuleConfig>>) exec)
                        .execute(workbook, fileConfig.getCellRules(), errors));

        if (fileConfig.getTableRules() == null) {
            log.debug("No tableRules configured for fileType={}", fileConfig.getFileType());
        } else {
            log.debug("Found {} tableRules for fileType={}", fileConfig.getTableRules().size(), fileConfig.getFileType());
        }

        ruleExecutorRegistry
                .get("tableRules")
                .ifPresent(exec -> ((RuleExecutor<List<com.example.excelvalidator.model.validation.v2.TableRuleConfig>>) exec)
                        .execute(workbook, fileConfig.getTableRules(), errors));

        int totalChecks =
                calculateTotalChecks(
                        fileConfig
                );

        int failedChecks =
                errors.size();

        int passedChecks =
                Math.max(
                        0,
                        totalChecks - failedChecks
                );

        result.setFileName(
                fileName
        );

        result.setFileType(
                fileConfig.getFileType()
        );

        result.setErrors(
                errors
        );

        result.setTotalChecks(
                totalChecks
        );

        result.setPassedChecks(
                passedChecks
        );

        result.setFailedChecks(
                failedChecks
        );

        result.setErrorCount(
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

    private FileValidationResult buildMissingSheetResult(
            String fileName,
            List<String> missingSheets
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

        result.setErrorCount(
                1
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

        result.setErrors(
                new ArrayList<>()
        );

        result.setMessage(
                "Missing required sheets: "
                        + String.join(
                        ", ",
                        missingSheets
                )
        );

        return result;
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

        result.setErrorCount(
                1
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

        result.setErrors(
                new ArrayList<>()
        );

        result.setMessage(
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Unexpected validation error"
        );

        return result;
    }

    private int calculateTotalChecks(
            FileRuleConfig fileConfig
    ) {

        int totalChecks = 0;

        if (
                fileConfig.getRequiredSheets()
                        != null
        ) {

            totalChecks +=
                    fileConfig.getRequiredSheets()
                            .size();
        }

        if (
                fileConfig.getCellRules()
                        != null
        ) {

            totalChecks +=
                    fileConfig.getCellRules()
                            .size();
        }

        if (
                fileConfig.getTableRules()
                        != null
        ) {

            totalChecks +=
                    fileConfig.getTableRules()
                            .size();
        }

        return totalChecks;
    }
}