# Copilot Coding Agent Onboarding — android-core

> **Read `AGENTS.md` first** for module map and conventions. This file covers build, CI, and validation.

## Overview
Infomaniak Android Core — a modular shared library consumed by all Infomaniak Android apps via **Gradle Composite Builds**. Contains ~25 independent modules (Auth, Network, Ui, Ktor, Notifications, etc.) plus a `Legacy` module for backward compatibility.

## Build & Test (CI: `.github/workflows/android.yml`)
```bash
./gradlew clean
./gradlew build --stacktrace
./gradlew testDebugUnitTest --stacktrace
```

## How Apps Consume This Library
```kotlin
// In host app settings.gradle.kts:
pluginManagement { includeBuild("Core/build-logic") }
plugins { id("com.infomaniak.core.composite") }
```
The plugin substitutes `com.infomaniak.core:*` GAV coords with local project paths. See `CoreCompositePlugin.kt` for the full mapping.

## Project Layout
```
<ModuleName>/           # One directory per module (Auth, Ktor, Ui, Login, Matomo…)
Legacy/                 # Backward-compatible monolithic module
build-logic/
├── composite/          # CoreCompositePlugin (settings plugin for host apps)
└── convention/         # Shared Gradle convention plugins
gradle/libs.versions.toml
gradle/core.versions.toml  # Exposed to host apps
lint.xml                   # Shared lint config — applies to all modules
```

## PR Review Instructions

- Ensure strings are localized via `strings.xml` resources where user-visible text is needed.
- Each module is independently consumed by apps — avoid introducing circular dependencies between modules.
- Public API changes in any module may break all consuming apps — document breaking changes clearly in the PR description.
- `lint.xml` at root applies to all modules — ensure `./gradlew lint` passes before opening a PR.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
