# Project Structure

This project organizes Java classes and models by role, with a clear separation between active validation rules and legacy support.

## Packages

- `com.example.excelvalidator.controller`
  - REST endpoints and exception handling.
  - Primary classes: `ExcelController`, `HealthController`, `ApiExceptionHandler`.

- `com.example.excelvalidator.service`
  - Business logic and orchestration.
  - Key classes: `ExcelValidationService`, `RuleExecutorRegistry`, `RuleExecutor`, executor adapters, and validation engines.

- `com.example.excelvalidator.model`
  - DTOs and response types.
  - Key classes: `CellValidationError`, `ExcelValidationResponse`, `FileValidationResult`, `BatchValidationResponse`.

- `com.example.excelvalidator.model.validation.v2`
  - Active rule configuration model.
  - Key classes: `ValidationConfig`, `FileRuleConfig`, `CellRuleConfig`, `TableRuleConfig`, `TableColumnConfig`, `RowValidationRuleConfig`, `EndConditionConfig`, `SettingsConfig`.

- `com.example.excelvalidator.model.validation`
  - Legacy rule configuration model.
  - Key classes: `ValidationConfig`, `SheetRuleConfig`, `ColumnRuleConfig`, `RuleConfig`.

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
4. Optionally move executor classes into `service/executor` and engines into `service/engine` for clearer separation.

## Visual structure

- `src/main/java/com/example/excelvalidator`
  - `ExcelValidatorApplication.java`
  - `controller/`
  - `service/`
    - `ExcelValidationService.java`
    - `RuleExecutor.java`
    - `RuleExecutorRegistry.java`
    - `WorkbookRuleExecutor.java`
    - `CellRuleExecutor.java`
    - `TableRuleExecutor.java`
    - `WorkbookRuleEngine.java`
    - `CellRuleEngine.java`
    - `TableRuleEngine.java`
    - `JsonRuleEngine.java`
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
