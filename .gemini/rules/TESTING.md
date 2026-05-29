# Testing Rules

- **Framework**: Use JUnit 4 for unit tests and MockK for mocking.
- **Pattern**: Follow the **Given-When-Then** (GWT) structure for all test cases.
- **Coroutines**: Use `runTest` and `UnconfinedTestDispatcher` for testing coroutines and Flows.
- **State Testing**: Use the **Turbine** library to test `StateFlow` emissions in ViewModels.
- **Coverage**: Prioritize 100% coverage for `UseCases` and `Mappers`.
- **Naming**: Use descriptive test names in backticks, e.g., `` `GIVEN a valid id WHEN fetch is called THEN return character` ``.
