# PrivacyManagement Module

## Description
The `PrivacyManagement` module is a reusable UI library designed to manage privacy settings within the application. Its main purpose is to list the tracking tools used (such as Sentry or Matomo) and provide a user interface to enable or disable data collection for each of them.

## Features
- **Tracker List**: An interface to display all configured tracking services.
- **Tracker Details**: A detailed view for each tracker explaining its purpose.
- **Consent Management**: A switch allowing users to grant or withdraw their consent.
- **Flexible Theming**: Uses `CompositionLocal` to adapt the module's visual style to the host application.

## Installation
Add the module dependency to your `build.gradle.kts` file:

```kotlin
implementation(project(":Core:PrivacyManagement"))
```
Or
```kotlin
implementation(core.infomaniak.core.privacymanagement)
```

## Usage

### 1. Display the tracker list
Use `PrivacyManagementHomeContent` to display the list of available services.

```kotlin
PrivacyManagementHomeContent(
    trackerList = persistentListOf(Tracker.Sentry, Tracker.Matomo),
    onTrackerClick = { tracker ->
        // Navigate to the tracker detail page
    },
    header = { /* Optional: Illustration or title */ },
    divider = { HorizontalDivider() },
    rightIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
)
```

### 2. Display the tracker detail page
Use `PrivacyManagementTrackerContent` to allow users to change their preferences.

```kotlin
PrivacyManagementTrackerContent(
    tracker = Tracker.Sentry,
    isTrackerEnabled = { viewModel.isSentryEnabled },
    onTrackerSwitchClick = { isEnabled ->
        viewModel.toggleSentry(isEnabled)
    }
)
```

### 3. Theme Customization
The module uses `LocalPrivacyManagementTheme` to define its appearance (container colors, padding, shapes). You can override it at your screen level:

```kotlin
val customTheme = PrivacyManagementTheme(
    trackerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    trackerContainerContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    trackerContainerPadding = PaddingValues(16.dp),
    trackerContainerShape = RoundedCornerShape(12.dp)
)

CompositionLocalProvider(LocalPrivacyManagementTheme provides customTheme) {
    PrivacyManagementHomeContent(...)
}
```

## Module Structure
- `tracker/`: Contains the `Tracker` enum defining texts and icons for each service.
- `screencontent/`: Contains the main Compose components (`HomeContent` and `TrackerContent`).
- `theme/`: Manages visual customization of the module.
- `images/`: Contains icons and illustrations specific to the tracking services.
