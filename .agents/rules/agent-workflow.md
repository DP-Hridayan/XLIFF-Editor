# Agent Implementation Workflow

Follow these phases sequentially when implementing new features to ensure stability and architectural compliance.

## 1. Research & Planning
- Analyze requirements and read existing related files.
- Create an initial overarching `implementation_plan.md` artifact. 
- When detailing individual phases, create **new separate plan artifacts** (e.g., `phase2_plan.md`). **Do NOT override or overwrite existing plans.**
- **STOP** and wait for user approval before modifying code.

## 2. Domain & Data (Core)
- Implement `domain` (models, interfaces, use cases).
- Implement `data` (API calls, DB entities, repositories).
- **Pragmatic TDD**: Follow Test Driven Development for core business logic (Use Cases, ViewModels, and utility functions). Write a failing test first, then write the minimal code to pass, then refactor (Red-Green-Refactor). Pure UI components (Compose) can bypass TDD.
- Compile (`./gradlew assembleDebug`) to verify core logic.

## 3. Presentation (UI & State)
- Implement `viewmodel` and UI state models.
- Build small, stateless UI elements in `presentation/components/`.
- Assemble components in `presentation/screens/`.
- Maintain strict State Hoisting.

## 4. Wiring & Verification
- Wire dependencies in `di/` (Hilt).
- Run final compilation and verify functionality.
- Create `walkthrough.md` summarizing changes.
