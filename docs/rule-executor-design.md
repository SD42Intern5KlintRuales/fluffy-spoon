Rule Executor Design
====================

Goal
----
Provide a small, extensible Java API so the existing validation engines can be invoked via a registry-driven executor. This separates "what to validate" (configuration) from "how to validate" (executor implementations) and enables adding new rule types without changing controller/service logic.

Core concepts
-------------
- `RuleExecutor<T>`: interface implemented by adapters that know how to run a specific rule model (e.g. cell rules, table rules, workbook-level checks).
- `RuleExecutorRegistry`: Spring component that maps a logical rule key (or rule class) to the corresponding `RuleExecutor` implementation.
- Executor adapters: thin classes that adapt existing engines (`CellRuleEngine`, `TableRuleEngine`, `WorkbookRuleEngine`, `JsonRuleEngine`) to the `RuleExecutor` interface.

Recommended Java API (illustrative)
----------------------------------

public interface RuleExecutor<T> {
    String getKey(); // e.g. "requiredSheets", "cellRules", "tableRules"
    void execute(Workbook workbook, T ruleObject, List<CellValidationError> errors);
    default int estimateChecks(T ruleObject) { return 0; }
}

Example registry
----------------

@Component
public class RuleExecutorRegistry {
    private final Map<String, RuleExecutor<?>> executors;

    @Autowired
    public RuleExecutorRegistry(List<RuleExecutor<?>> executorList) {
        this.executors = executorList.stream()
            .collect(Collectors.toMap(RuleExecutor::getKey, Function.identity()));
    }

    public Optional<RuleExecutor<?>> get(String key) {
        return Optional.ofNullable(executors.get(key));
    }
}

Adapters (examples)
-------------------
- `CellRuleExecutor` implements `RuleExecutor<List<CellRuleConfig>>` and delegates to `CellRuleEngine.validateCellRules`.
- `TableRuleExecutor` implements `RuleExecutor<List<TableRuleConfig>>` and delegates to `TableRuleEngine.validateTables`.
- `WorkbookRuleExecutor` implements `RuleExecutor<List<String>>` and delegates to `WorkbookRuleEngine.validateRequiredSheets`.

Integration changes (minimal)
-----------------------------
- Add executor adapters (one per existing engine).
- Auto-wire `RuleExecutorRegistry` into `ExcelValidationService`.
- Replace the direct calls to `workbookRuleEngine`, `cellRuleEngine`, `tableRuleEngine` with a small loop that iterates configured rule sections and looks up executors by key:

    - `registry.get("requiredSheets").execute(workbook, fileConfig.getRequiredSheets(), errors);`
    - `registry.get("cellRules").execute(workbook, fileConfig.getCellRules(), errors);`
    - `registry.get("tableRules").execute(workbook, fileConfig.getTableRules(), errors);`

Backward compatibility
----------------------
- Existing engines remain unchanged; adapters delegate to them.
- `ExcelValidationService` behavior is preserved but now driven by the registry; this makes it easy to add new executors for new rule types.

Testing
-------
- Unit test each adapter to assert it invokes the wrapped engine correctly and reports errors.
- Integration test: a small JSON `ValidationConfig` and an example workbook asserting expected pass/fail and aggregated counts.

Next steps
----------
- Implement adapter classes for current engines.
- Implement `RuleExecutorRegistry` and wire it into `ExcelValidationService`.
- Add tests and an example JSON `ValidationConfig` to `src/test/resources`.
