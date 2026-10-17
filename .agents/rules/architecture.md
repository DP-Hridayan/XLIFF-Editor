# Architecture Rules

This project follows a strict **Clean Architecture** pattern, emphasizing separation of concerns, testability, and modularity.

## Directory Structure
Feature modules should be structured into the following layers:
- **`data/`**: Data sources (e.g., `local` for Room DB, `remote` for APIs), repository implementations, parsers, and data transfer objects (DTOs).
- **`domain/`**: Business logic. Contains interfaces for repositories and managers, use cases, and core domain models. This layer must remain independent of Android framework dependencies and data/presentation details.
- **`presentation/`**: UI components. Contains `screens`, `components`, `viewmodel`, and UI state models.
- **`di/`**: Hilt dependency injection modules.

## Module Dependencies
To maintain strict modularization and prevent circular dependencies or spaghetti code:
- **Feature Modules**: Feature modules (`feature/*`) must **never** depend on each other. If features need to communicate, they should do so via a shared `core` module or through the `app` module via navigation/interfaces.
- **Core Modules**: Core modules (`core/*`) must **never** depend on any feature module. Core modules represent the foundational layer and should remain completely agnostic of feature implementations.

## UI Architecture (Jetpack Compose)
- **Single Source of Truth**: State must be hoisted. ViewModels manage the state, and UI components observe it.
- **Stateless Components**: Extract complex UI layouts into small, focused, and stateless components (placed in a `components` sub-package) to prevent massive "God-level" screen files.
- **No Side Effects in Composables**: Keep side effects out of UI components and delegate them to ViewModels or proper side-effect handlers (like `LaunchedEffect`).
- **State Hoisting**: Pass down only necessary state and callback lambdas to child components.

## Dependency Injection
- Use **Hilt** for dependency injection. 
- Inject dependencies via constructors (`@Inject constructor`).

## Data Flow
- Unidirectional Data Flow (UDF) is mandatory. The UI triggers events to the ViewModel, the ViewModel updates the state, and the UI re-renders based on the new state.

## Error Handling
- **Result Wrappers**: The Domain and Data layers must never throw raw exceptions into the ViewModel or UI. 
- Every repository or use-case method that can fail must return a wrapped result (e.g., `Result<T>` or a sealed class with `Success`, `Error`, and `Loading` states). This forces compile-time error handling and prevents unexpected crashes.
