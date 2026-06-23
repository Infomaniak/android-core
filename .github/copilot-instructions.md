# Copilot Coding Agent Onboarding — android-core

> **Read `AGENTS.md` first** for module map and conventions. This file covers build, CI, and validation.

## Overview
Infomaniak Android Core — a modular shared library consumed by all Infomaniak Android apps via **Gradle Composite Builds**. Contains ~25 independent modules (Auth, Network, Ui, Ktor, Notifications, etc.) plus a `Legacy` module for backward compatibility.

## One-Time Setup
No submodules. No env file needed.

## Build & Test (CI: `.github/workflows/android.yml`)
```bash
./gradlew clean
./gradlew build --stacktrace           # compiles all modules
./gradlew testDebugUnitTest --stacktrace
```
CI runs these three steps on every non-draft PR.

## How Apps Consume This Library
Apps include Core as a **composite build** (not a published artifact). In the host app's `settings.gradle.kts`:
```kotlin
pluginManagement { includeBuild("Core/build-logic") }
plugins { id("com.infomaniak.core.composite") }
```
The plugin auto-substitutes `com.infomaniak.core:*` GAV coordinates with local project paths. See `build-logic/composite/src/main/kotlin/com/infomaniak/core/composite/CoreCompositePlugin.kt` for the full mapping.

## Project Layout
```
<ModuleName>/           # One directory per module (Auth, Ktor, Ui, Login, Matomo…)
Legacy/                 # Backward-compatible monolithic module
build-logic/
├── composite/          # CoreCompositePlugin (settings plugin for host apps)
└── convention/         # Shared Gradle convention plugins
gradle/
├── libs.versions.toml  # Main version catalog
└── core.versions.toml  # Exposed to host apps
lint.xml                # Shared lint config (applied to all modules)
```

## Key Rules
- Each module is independently publishable — avoid circular dependencies between modules.
- `lint.xml` at root applies to all modules; keep lint passing (`./gradlew lint`).
- Public API changes in any module may break consuming apps — communicate clearly in PR description.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
