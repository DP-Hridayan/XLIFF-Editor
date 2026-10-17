# Coding Style Rules

This document outlines the strict coding style and formatting conventions to be followed in this project.

## 1. Comments and Documentation
- **No Inline Comments**: Absolutely no inline comments or block comments explaining what the code does. Code should be self-documenting through clear, descriptive naming.
- **KDocs Allowed**: KDoc comments are allowed and encouraged *only* where necessary (e.g., documenting public APIs, complex interfaces, or core domain logic).

## 2. Function Size and Responsibility
- **No God Functions**: Functions must be very small and strictly adhere to the Single Responsibility Principle.
- **Minimal Responsibility**: A function should do exactly one thing. If a function grows too large or handles multiple concerns, it must be extracted into smaller, private helper functions or delegated to separate utility classes.

## 3. Variables and State (Strict Immutability)
- **Prefer `val`**: Always prefer `val` over `var` whenever possible.
- **Immutable Collections**: Publicly exposed collections must always be immutable (e.g., expose `StateFlow<List<T>>` instead of `MutableStateFlow<MutableList<T>>`).

## 4. Magic Numbers and Strings
- **No Magic Values**: There must be absolutely no "magic numbers" for things like business logic like delays, ids, tags. etc (e.g., `delay(1500)`) or hardcoded backend keys/flags inline in the code. 
- **Constants**: Extract them into `private const val` declarations at the top of the file, or into a dedicated `Constants` object.

## 5. String Localization
- **No Hardcoded Strings**: Strings that need localization must never be hardcoded in the codebase.
- **Resource Location**: All translatable strings must be added to the `core/resources` module (`strings.xml`).
- **String Reuse & Naming Strategy**: 
  - Before adding a string, **always check if the exact value or identifier already exists**. 
  - Prioritize generic identifiers. For example, if the value is "Use", the identifier should be `use`, not `chat_screen_use`. 
  - Identifiers must exactly match (or be as close as possible to) the actual string value.
  - *Exception*: This identifier naming rule does not apply to long phrases, messages, summaries, instructions, or steps. For these, use descriptive identifiers.
- **Crowdin Context**: When adding new strings to `strings.xml`, you must provide context for translators using the `comment` XML attribute.
  - **Format**: `<string name="add_new_item" comment="Generic button label to add a new list entry">Add new item</string>`
  - **Exception**: You may omit the `comment` attribute *only* if the string is very short, highly generic, and completely self-explanatory without having multiple potential meanings (e.g., the string "Add").

## 4. Dependencies and Gradle
- **Version Catalogs (TOML)**: When adding new dependencies, you must use TOML syntax (Gradle Version Catalogs). Entries in the toml must be put to follow the alphabetical order. 
- **No Hardcoding**: Do not hardcode dependency strings directly in `build.gradle.kts` files.
- **Variables**: Use version variables (`[versions]`) within the TOML file for dependency versions.

## 6. Imports and Naming
- **Full Imports Only**: When using any code, method, or object, you must make a full import in the import section at the top of the file.
- **No Wildcard Imports**: Never use wildcard imports (e.g., `import java.util.*`). Every single class must be imported explicitly to prevent naming collisions.
- **No Explicit Paths**: Do not add explicit fully qualified paths directly in the inline code (it looks ugly and causes bloat).
- **Ambiguity Exception**: The *only* exception to this rule is if using a fully qualified path is absolutely required to resolve naming ambiguity.

## 7. Code Spacing and Formatting
- **Meaningful Whitespace**: Use newlines to separate logical blocks of code. Do not cram line after line together.
- **Related Statements**: Newlines are not needed when things are closely related, such as a block of variable definitions.
