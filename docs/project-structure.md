# Project Structure

This project organizes Java classes and models by role, with a clear separation between active validation rules and legacy support.

## Packages

- `com.example.excelvalidator.controller`
  - REST endpoints and exception handling.
  - Primary classes: `ExcelController`, `HealthController`, `ApiExceptionHandler`.

- `com.example.excelvalidator.service`
  - Business logic and orchestration.
  - Key classes: `ExcelValidationService`, `RuleExecutorRegistry`, and `RuleExecutor`.

- `com.example.excelvalidator.service.executor`
  - Adapter classes that map rule sections to validation engines.
  - Key classes: `WorkbookRuleExecutor`, `CellRuleExecutor`, `TableRuleExecutor`.

- `com.example.excelvalidator.service.engine`
  - Core validation logic implementations.
  - Key classes: `WorkbookRuleEngine`, `CellRuleEngine`, `TableRuleEngine`.

- `com.example.excelvalidator.model`
  - DTOs and response types.
  - Key classes: `CellValidationError`, `ExcelValidationResponse`, `FileValidationResult`, `BatchValidationResponse`.

- `com.example.excelvalidator.model.validation.v2`
  - Active rule configuration model.
  - Key classes: `ValidationConfig`, `FileRuleConfig`, `CellRuleConfig`, `TableRuleConfig`, `TableColumnConfig`, `RowValidationRuleConfig`, `EndConditionConfig`, `SettingsConfig`.

- `com.example.excelvalidator.model.validation`
  - Legacy rule configuration model.
  - Key classes: `ValidationConfig`, `SheetRuleConfig`, `ColumnRuleConfig`, `RuleConfig`.

## Architecture Layers

This project is organized into explicit layers for clarity and maintainability:

- **API layer**
  - `ExcelController` receives requests and forwards them to `ExcelValidationService`.
- **Rule execution layer**
  - `RuleExecutorRegistry` dispatches configured validation sections to the appropriate executor adapters.
  - `WorkbookRuleExecutor`, `CellRuleExecutor`, and `TableRuleExecutor` adapt active configuration sections into engine invocations.
- **Engine layer**
  - `WorkbookRuleEngine`, `CellRuleEngine`, and `TableRuleEngine` contain the actual validation logic.
- **Model layer**
  - Validation config models, response DTOs, and legacy schema support.

## Why `model.validation.v2` exists

The `v2` package is the active validation schema. It is intentionally separated because:

- it supports the newer `FileRuleConfig` / `tableRules` / `cellRules` structure;
- it is the model currently used by `ExcelValidationService` and its executor-based rule flow;
- the legacy `model.validation` classes are kept only for backward compatibility with `JsonRuleEngine` and older config JSON.

If you want to clean the project further, you can delete `model.validation` once the legacy JSON path is no longer required.

## Recommended cleanup path

1. Keep `model.validation.v2` as the active schema.
2. Keep `model.validation` only while legacy support is still needed.
3. Add a short note in `README.md` explaining that v1 is legacy and v2 is current.
4. Executor classes are in `service/executor` and engines are in `service/engine` for clearer separation.

## Visual structure

- `src/main/java/com/example/excelvalidator`
  - `ExcelValidatorApplication.java`
  - `controller/`
  - `service/`
    - `ExcelValidationService.java`
    - `RuleExecutor.java`
    - `RuleExecutorRegistry.java`
    - `JsonRuleEngine.java`
    - `executor/`
      - `WorkbookRuleExecutor.java`
      - `CellRuleExecutor.java`
      - `TableRuleExecutor.java`
    - `engine/`
      - `WorkbookRuleEngine.java`
      - `CellRuleEngine.java`
      - `TableRuleEngine.java`
  - `model/`
    - `CellValidationError.java`
    - `ExcelValidationResponse.java`
    - `response/`
    - `validation/`
      - `ValidationConfig.java` (legacy)
      - `SheetRuleConfig.java`
      - `ColumnRuleConfig.java`
      - `RuleConfig.java`
      - `v2/`
        - `ValidationConfig.java`
        - `FileRuleConfig.java`
        - `CellRuleConfig.java`
        - `TableRuleConfig.java`
        - `TableColumnConfig.java`
        - `RowValidationRuleConfig.java`
        - `EndConditionConfig.java`
        - `SettingsConfig.java`

## What to watch for

- `ExcelValidationService` uses `model.validation.v2.ValidationConfig`.
- `JsonRuleEngine` uses the legacy `model.validation` types.
- If `JsonRuleEngine` is no longer required, remove the legacy package.
