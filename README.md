# XLIFF Editor

An Android app to view, edit, and translate Android `strings.xml` and standard XLIFF (`.xliff`, `.xlf`) localization files.

## Features

- **File Editing**: Open and edit Android `strings.xml` and XLIFF (`.xliff`, `.xlf`) files on your device.
- **Translation Workflow**: View source and target strings side by side with status tracking (`Translated`, `Fuzzy / Needs Review`, `Untranslated`).
- **Validation**: Checks for missing format specifiers (`%1$s`, `%2$d`), unclosed tags, and unescaped quotes.
- **Search & Filter**: Find strings quickly by key name, text content, or translation status.
- **Material You UI**: Built with Jetpack Compose and Material 3 Expressive, supporting dynamic color, system light/dark mode, and AMOLED black.

## Tech Stack

- **UI**: Jetpack Compose, Material 3 Expressive
- **Language**: Kotlin 2.4.20
- **Dependency Injection**: Dagger Hilt
- **Architecture**: Clean Architecture (Data, Domain, Presentation)
- **Minimum Android Version**: Android 9.0 (API 28)

## Build

Clone the repository and build using Gradle:

```bash
git clone git@github.com:DP-Hridayan/XLIFF-Editor.git
cd XLIFF-Editor
./gradlew assembleRelease
```

## License

This project is licensed under the Apache License 2.0.
