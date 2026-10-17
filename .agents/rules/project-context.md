# Project Context: XLIFF Editor

## Overview
**XLIFF Editor** is an elegant, modern XML and XLIFF localization utility application for Android, built with Kotlin and Jetpack Compose. Its primary purpose is to help developers, translators, and mobile power users parse, edit, validate, and translate Android `strings.xml` and standard XLIFF (`.xliff`, `.xlf`) files.

## Core Capabilities
- **XML & XLIFF Editing & Translation**: Parse, view, edit, and translate Android string resources (`<string>`, `<plurals>`, `<string-array>`, CDATA, formatted, comments).
- **Multi-Locale Synchronization & Diffing**: Correlate base language (`values/strings.xml`) with target locales (`values-<lang>/strings.xml`), detecting missing strings (`Untranslated`), outdated changes (`Fuzzy / Needs Review`), and orphan keys.
- **Safety & Format Linter**: Rigorous verification of format specifiers (`%s`, `%d`, `%1$s`, `%.2f`), XML quote escaping (`\'`), and HTML/placeholder tag balancing.
- **Machine & AI Translation Integration**: Offline private on-device translation via Google ML Kit, context-aware AI translation via Google Gemini Flash, and standard REST translation APIs.
- **Material You Expressive UI**: Built using Material 3 Expressive APIs, featuring interactive bouncy cards (`CustomCard`), dynamic Monet color scheme, system light/dark following, AMOLED pure black mode, and smooth fluid animations matching the styling established in aShellYou.
- **SAF File Access**: Seamless reading and writing back to files and Android `res/` directory trees using the Storage Access Framework (`DocumentFile`).

## Tech Stack & Architecture
- **Platform**: Native Android (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0, JVM 17)
- **UI Toolkit**: Jetpack Compose (Compose BOM 2026.09.00, Material 3 1.5.0-alpha29 Expressive)
- **Architecture**: Strict Clean Architecture (Data, Domain, Presentation) with UDF.
- **Dependency Injection**: Dagger Hilt 2.60.1
- **Storage & Preferences**: Android DataStore Preferences, DocumentFile (SAF), Room DB for Translation Memory
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`), Kotlin DSL (`build.gradle.kts`)

## Key Principles for AI Agents
When working in this project:
- Strictly follow `.agents/rules/` at all times.
- No inline comments: write self-documenting code.
- Strict immutability: use `val` and immutable collections.
- No magic values: extract constants into designated objects or files.
- Localize all user-facing strings into `strings.xml` with Crowdin `comment` attributes.
- Use full imports only (no wildcard imports, no inline fully qualified paths).
- Result wrappers: Domain and Data layers return wrapped results, never throwing raw exceptions to UI.
- Pragmatic TDD: write unit tests first for parsers, use cases, and ViewModels.
