# Core - Infomaniak Android Library

## Package Identity

Modular Android library consumed via Gradle composite builds. Provides shared authentication, networking, UI components, and
utilities across Infomaniak apps. Designed for JDK 17+, minSdk 27, compiled SDK 35.

## Setup & Development

```bash
# Build all Core modules
./gradlew Core:assemble

# Run Core tests
./gradlew Core:test

# Lint Core with ktlint
./gradlew Core:ktlintCheck

# Build specific module
./gradlew Core:Auth:build
./gradlew Core:Network:build
```

## Module Categories

### Authentication & Security

- **Auth**: OAuth2 flow, account management, token storage
    - Key: `Auth/src/main/java/com/infomaniak/core/auth/`
    - Example: `AuthTokenRepository.kt` for token handling
- **TwoFactorAuth**: 2FA verification UI (Front) and backend (Back)
- **AppIntegrity**: App attestation and integrity checks

### Networking

- **Network**: Ktor HTTP client with interceptors
    - Key: `Network/Ktor/src/main/`
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
    - HTTP: `HttpClientProvider.kt` for Ktor client setup
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
- **Composite build**: Consume via Maven coordinates `com.infomaniak.core:<module>`
- **Ktlint**: Version 1.7.1, Android mode enabled
- **No main**: Libraries have no Application class

## Key Files

- Build plugin: `build-logic/composite/src/main/kotlin/CoreCompositePlugin.kt`
- Dependency catalog: `gradle/core.versions.toml`
- Shared values: `SharedValues/src/main/`

## JIT Index

### Find module implementations

```bash
# Auth utilities
rg -n "AuthToken|AccountManager" Core/Auth/

# Network interceptors
rg -n "Interceptor|RequestBuilder" Core/Network/

# Compose components
rg -n "@Composable" Core/Ui/Compose/

# Common extensions
rg -n "fun|suspend" Core/Common/src/main/java/com/infomaniak/core/common/
```

### Test file locations

```bash
# Unit tests per module
rg -n "@Test" Core/*/src/test/

# Example: Auth tests
find Core/Auth/src/test -name "*.kt"

# Example: Common utils tests
find Core/Common/src/test -name "*.kt"
```

## Common Gotchas

- **Composite build**: Changes in Core are immediately available in consuming projects
- **Dependency mapping**: Modules use Maven coordinates but resolve locally
- **Legacy init**: Must call `InfomaniakCore.init()` in consuming app's Application class
- **Ktor client**: Use `HttpClientProvider` from Common for consistent setup

## Pre-PR Checks

```bash
./gradlew Core:test && ./gradlew Core:ktlintCheck
```
