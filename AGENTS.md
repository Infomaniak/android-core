# Core - Infomaniak Android Library

## Package Identity

Modular Android library consumed via Gradle composite builds. Provides shared authentication, networking, UI components, and
utilities across Infomaniak apps. Designed for JDK 17+, minSdk 27, compiled SDK 35.

## Setup & Development

> **Note**: When this library is included as a Gradle composite build inside another project under the `:Core` path, all
> task paths below should be prefixed with `Core:` (e.g., `./gradlew Core:assemble`). When working in this repository
> standalone, omit the prefix.

```bash
# Build all modules
./gradlew assemble

# Run all tests
./gradlew test

# Lint with ktlint
./gradlew ktlintCheck

# Build a specific module
./gradlew :Auth:build
./gradlew :Network:build
```

## Module Categories

### Authentication & Security

- **Auth**: OAuth2 flow, account management, token storage
    - Key: `Auth/src/main/kotlin/com/infomaniak/core/auth/`
    - Examples: token/interceptor/authenticator classes (e.g., `TokenInterceptor.kt`, `TokenAuthenticator.kt`, `CredentialManager.kt`)
- **TwoFactorAuth**: 2FA verification UI (Front) and backend (Back)
- **AppIntegrity**: App attestation and integrity checks

### Networking

- **Network**: Ktor HTTP client with interceptors
    - Key: `Network/Ktor/src/main/`
    - HTTP client factory: `Network/Ktor/src/main/kotlin/CreateHttpClient.kt`
    - Models: `Network/Models/src/main/`
- **Ktor**: Low-level Ktor extensions

### UI Components

- **Ui:Compose**: Modern Compose components
    - Theme: `Ui/Compose/Theme/src/main/`
    - Components: `Ui/Compose/BottomStickyButtonScaffolds/`, `Basics/`
- **Ui:View**: Legacy XML components (SharedViews, EdgeToEdge)
- **CrossAppLogin**: Cross-application authentication

### Utilities & Features

- **Common**: Shared extensions, formatters, utilities
    - Formatters: `FormatterFileSize.kt`, date formatters
- **Coil**: Image loading configuration
- **Matomo**: Analytics tracking
- **Sentry**: Error reporting
- **Notifications**: Push notification handling
- **Stores**: In-app review and update support

### Legacy Support

- **Legacy**: Backward compatibility module
    - Use: `implementation(project(":Core:Legacy"))`
    - Init in Application: `InfomaniakCore.init(...)`

## Patterns & Conventions

- **Module naming**: Descriptive, PascalCase (e.g., `AppVersionChecker`, `TwoFactorAuth`)
- **Composite build**: Consume via Maven coordinates `com.infomaniak.core:<artifact>`, where nested Gradle project paths map `:` to `.` in the artifact name (e.g., `:TwoFactorAuth:Front` → `com.infomaniak.core:TwoFactorAuth.Front`)
- **Ktlint**: Android mode enabled (version set in `build.gradle.kts`)
- **No main**: Libraries have no Application class

## Key Files

- Build plugin: `build-logic/composite/src/main/kotlin/com/infomaniak/core/composite/CoreCompositePlugin.kt`
- Dependency catalog: `gradle/core.versions.toml`
- Shared values: `SharedValues/src/main/`

## JIT Index

### Find module implementations

```bash
# Auth utilities
rg -n "AuthToken|AccountManager" Auth/

# Network interceptors
rg -n "Interceptor|RequestBuilder" Network/

# Compose components
rg -n "@Composable" Ui/Compose/

# Common extensions
rg -n "fun|suspend" Common/src/main/kotlin/com/infomaniak/core/common/
```

### Test file locations

```bash
# Unit tests per module
rg -n "@Test" */src/test/

# Example: Auth tests
find Auth/src/test -name "*.kt"

# Example: Common utils tests
find Common/src/test -name "*.kt"
```

## Common Gotchas

- **Composite build**: Changes in Core are immediately available in consuming projects
- **Dependency mapping**: Modules use Maven coordinates but resolve locally
- **Legacy init**: Must call `InfomaniakCore.init()` in consuming app's Application class
- **Ktor client**: Use the canonical `createHttpClient(...)` factory under `Network/Ktor/src/main/kotlin/CreateHttpClient.kt` for consistent setup

## Pre-PR Checks

```bash
# Standalone (in this repository)
./gradlew test && ./gradlew ktlintCheck

# As composite build (prefix with Core: when consumed from another project)
./gradlew Core:test && ./gradlew Core:ktlintCheck
```
