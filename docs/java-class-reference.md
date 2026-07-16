**Java Class Reference**

This document enumerates the primary Java classes in the project with a short purpose statement and links to their source files.

- **Application & entrypoint**
  - `ExcelValidatorApplication`: [src/main/java/com/example/excelvalidator/ExcelValidatorApplication.java](src/main/java/com/example/excelvalidator/ExcelValidatorApplication.java#L1-L50) — Spring Boot application entrypoint.

- **Controllers**
  - `ExcelController`: [src/main/java/com/example/excelvalidator/controller/ExcelController.java](src/main/java/com/example/excelvalidator/controller/ExcelController.java#L1-L200) — HTTP endpoints for single-file and batch validation; calls `ExcelValidationService`.
  - `HealthController`: [src/main/java/com/example/excelvalidator/controller/HealthController.java](src/main/java/com/example/excelvalidator/controller/HealthController.java#L1-L80) — basic health-check endpoint.
  - `ApiExceptionHandler`: [src/main/java/com/example/excelvalidator/controller/ApiExceptionHandler.java](src/main/java/com/example/excelvalidator/controller/ApiExceptionHandler.java#L1-L200) — centralized exception handling for controller errors.

- **Service layer**
  - `ExcelValidationService`: [src/main/java/com/example/excelvalidator/service/ExcelValidationService.java](src/main/java/com/example/excelvalidator/service/ExcelValidationService.java#L1-L999) — main orchestration service: loads rules, finds matching `FileRuleConfig`, dispatches rule sections to executors, aggregates results for responses.
  - `RuleExecutor`: [src/main/java/com/example/excelvalidator/service/RuleExecutor.java](src/main/java/com/example/excelvalidator/service/RuleExecutor.java#L1-L200) — generic interface for rule executors (adapters).
  - `RuleExecutorRegistry`: [src/main/java/com/example/excelvalidator/service/RuleExecutorRegistry.java](src/main/java/com/example/excelvalidator/service/RuleExecutorRegistry.java#L1-L200) — Spring component that maps executor keys (e.g. `cellRules`, `tableRules`) to `RuleExecutor` implementations.

- **Executor adapters**
  - `WorkbookRuleExecutor`: [src/main/java/com/example/excelvalidator/service/WorkbookRuleExecutor.java](src/main/java/com/example/excelvalidator/service/WorkbookRuleExecutor.java#L1-L200) — adapter that delegates required-sheet checks to `WorkbookRuleEngine`.
  - `CellRuleExecutor`: [src/main/java/com/example/excelvalidator/service/CellRuleExecutor.java](src/main/java/com/example/excelvalidator/service/CellRuleExecutor.java#L1-L200) — adapter that delegates cell checks to `CellRuleEngine`.
  - `TableRuleExecutor`: [src/main/java/com/example/excelvalidator/service/TableRuleExecutor.java](src/main/java/com/example/excelvalidator/service/TableRuleExecutor.java#L1-L200) — adapter that delegates table checks to `TableRuleEngine`.

- **Engines (validation logic)**
  - `WorkbookRuleEngine`: [src/main/java/com/example/excelvalidator/service/WorkbookRuleEngine.java](src/main/java/com/example/excelvalidator/service/WorkbookRuleEngine.java#L1-L200) — validates presence of required sheets.
  - `CellRuleEngine`: [src/main/java/com/example/excelvalidator/service/CellRuleEngine.java](src/main/java/com/example/excelvalidator/service/CellRuleEngine.java#L1-L300) — validates single-cell rules such as `REQUIRED` and `REQUIRED_DATE`.
  - `TableRuleEngine`: [src/main/java/com/example/excelvalidator/service/TableRuleEngine.java](src/main/java/com/example/excelvalidator/service/TableRuleEngine.java#L1-L500) — validates tables (rows) using `TableRuleConfig` and row-validation rules; includes fallback to find first non-empty row when `startRow` is past sheet end.
  - `JsonRuleEngine`: [src/main/java/com/example/excelvalidator/service/JsonRuleEngine.java](src/main/java/com/example/excelvalidator/service/JsonRuleEngine.java#L1-L400) — legacy engine that validates sheet/column rules defined by the older JSON model (v1).

- **Models / DTOs**
  - `CellValidationError`: [src/main/java/com/example/excelvalidator/model/CellValidationError.java](src/main/java/com/example/excelvalidator/model/CellValidationError.java#L1-L80) — record representing a single validation failure (sheet, row, column, field, value, message).
  - `ExcelValidationResponse`: [src/main/java/com/example/excelvalidator/model/ExcelValidationResponse.java](src/main/java/com/example/excelvalidator/model/ExcelValidationResponse.java#L1-L80) — response record for single-file validation.
  - `FileValidationResult`: [src/main/java/com/example/excelvalidator/model/response/FileValidationResult.java](src/main/java/com/example/excelvalidator/model/response/FileValidationResult.java#L1-L200) — file-level aggregated validation result used in batch responses.
  - `BatchValidationResponse`: [src/main/java/com/example/excelvalidator/model/response/BatchValidationResponse.java](src/main/java/com/example/excelvalidator/model/response/BatchValidationResponse.java#L1-L200) — aggregated results for batch uploads.

- **Validation config models (v2)**
  - `ValidationConfig` (v2): [src/main/java/com/example/excelvalidator/model/validation/v2/ValidationConfig.java](src/main/java/com/example/excelvalidator/model/validation/v2/ValidationConfig.java#L1-L200) — top-level active configuration for validation rules (files array). This is the model currently used by `ExcelValidationService` and the rule executor flow.
  - `FileRuleConfig`: [src/main/java/com/example/excelvalidator/model/validation/v2/FileRuleConfig.java](src/main/java/com/example/excelvalidator/model/validation/v2/FileRuleConfig.java#L1-L200) — per-file configuration (requiredSheets, cellRules, tableRules).
  - `CellRuleConfig`: [src/main/java/com/example/excelvalidator/model/validation/v2/CellRuleConfig.java](src/main/java/com/example/excelvalidator/model/validation/v2/CellRuleConfig.java#L1-L200) — single-cell rule model.
  - `TableRuleConfig`, `TableColumnConfig`, `RowValidationRuleConfig`, `EndConditionConfig`, `SettingsConfig` — models for table rules and settings in `model/validation/v2`.

- **Legacy validation config models (v1)**
  - `ValidationConfig`: [src/main/java/com/example/excelvalidator/model/validation/ValidationConfig.java](src/main/java/com/example/excelvalidator/model/validation/ValidationConfig.java#L1-L200) — legacy validation config used only by `JsonRuleEngine`.
  - `SheetRuleConfig`, `ColumnRuleConfig`, `RuleConfig`: legacy sheet/column model classes under `src/main/java/com/example/excelvalidator/model/validation/`.
  - `JsonRuleEngine`: [src/main/java/com/example/excelvalidator/service/JsonRuleEngine.java](src/main/java/com/example/excelvalidator/service/JsonRuleEngine.java#L1-L400) — legacy engine for the older v1 JSON schema.

**How the pieces fit (high level)**
- The controller (`ExcelController`) accepts uploads and calls `ExcelValidationService`.
- `ExcelValidationService` loads the JSON `ValidationConfig`, finds a matching `FileRuleConfig`, then iterates configured rule sections and dispatches each section to the appropriate `RuleExecutor` obtained from `RuleExecutorRegistry`.
- The `RuleExecutor` adapters delegate to the corresponding engine (`CellRuleEngine`, `TableRuleEngine`, `WorkbookRuleEngine`) which perform the actual workbook/sheet/cell/row checks and record `CellValidationError` entries.
- Results are aggregated into `FileValidationResult` and `BatchValidationResponse` and returned to the caller.

See the class diagram for a compact view of relationships:

See [docs/class-diagram.md](docs/class-diagram.md) for the diagram.
