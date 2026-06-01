# InAppUpdate Module Documentation

The `InAppUpdate` module provides a lifecycle-aware abstraction layer for in-app update flows.  
It supports two distribution flavors:

- **`standard`** — Google Play in-app update API (flexible & immediate update types), with optional WorkManager-based completion of downloaded flexible updates
- **`fdroid`** — F-Droid update detection via the Infomaniak API
## 1. Integration (Gradle)

Add the following dependency to your `build.gradle.kts`:

```kotlin
implementation(project(":Core:InAppUpdate"))
```

or with a composite build:

```kotlin
implementation(core.infomaniak.core.inappupdate)
```

Make sure your module declares the `distribution` flavor dimension and includes both `standard` and `fdroid` product flavors to match the ones defined in this module.

---

## 2. Setup

### 2.1 Initialize the manager

Create an `InAppUpdateManager` instance in your `ComponentActivity` and call `init()`. The manager registers itself as a `DefaultLifecycleObserver` and will automatically check for updates on each `onStart`.

```kotlin
class MainActivity : ComponentActivity() {

    private val inAppUpdateManager by lazy { InAppUpdateManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inAppUpdateManager.init(
            isUpdateRequired = false,           // true → IMMEDIATE update, false → FLEXIBLE
            onUserChoice = { accepted -> },     // called when the user accepts/dismisses the Play dialog
            onInstallStart = { },               // called just before completing installation
            onInstallFailure = { exception -> },
            onInstallSuccess = { },
            // (standard flavor only) emits true/false when a downloaded update is ready to install
            onInAppUpdateUiChange = { isReady -> showInstallBanner(isReady) },
            // (fdroid flavor only) emits true when a newer version is available on F-Droid
            onFDroidResult = { hasUpdate -> showFdroidBanner(hasUpdate) },
        )
    }
}
```

> **Important:** `init()` must be called before the activity reaches the `STARTED` state, i.e., inside `onCreate`.

---

## 3. Flexible update (standard flavor)

A flexible update downloads in the background and requires the user to explicitly install it.  
The module signals when the download is complete via `onInAppUpdateUiChange(true)`.

### 3.1 Show the "update available" bottom sheet

Use the `UpdateAvailableBottomSheetContent` composable inside a `ModalBottomSheet` or any container of your choice.

```kotlin
@Composable
fun UpdateBottomSheet(
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        UpdateAvailableBottomSheetContent(
            illustration = painterResource(R.drawable.il_update),
            titleTextStyle = MaterialTheme.typography.headlineSmall,
            descriptionTextStyle = MaterialTheme.typography.bodyMedium,
            installUpdateButton = { modifier ->
                Button(onClick = onInstall, modifier = modifier) {
                    Text(text = stringResource(R.string.updateInstall))
                }
            },
            dismissButton = { modifier ->
                OutlinedButton(onClick = onDismiss, modifier = modifier) {
                    Text(text = stringResource(R.string.later))
                }
            },
        )
    }
}
```

### 3.2 Install the downloaded update

Trigger installation when the user confirms:

```kotlin
inAppUpdateManager.installDownloadedUpdate()
```

### 3.3 Collect the download state in Compose

```kotlin
val canInstall by inAppUpdateManager.canInstallUpdate.collectAsState(initial = false)

if (canInstall) {
    UpdateBottomSheet(
        onInstall = { inAppUpdateManager.installDownloadedUpdate() },
        onDismiss = { /* dismiss */ },
    )
}
```

---

## 4. Mandatory update (standard flavor)

When `isUpdateRequired = true`, the manager uses `AppUpdateType.IMMEDIATE` and the update dialog cannot be dismissed.  
If the server reports that the current version is below the minimum supported version, launch `UpdateRequiredActivity`.

### 4.1 Check from the server whether an update is required

Use the companion extension `checkUpdateIsRequired` inside a `FragmentActivity`:

```kotlin
class MainActivity : FragmentActivity() {

    private val inAppUpdateManager by lazy { InAppUpdateManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkUpdateIsRequired(
            manager = inAppUpdateManager,
            applicationId = BuildConfig.APPLICATION_ID,
            applicationVersionCode = BuildConfig.VERSION_CODE,
            theme = R.style.AppTheme,
        )
    }
}
```

This collects `isUpdateRequired` while the activity is at least `STARTED` and automatically navigates to `UpdateRequiredActivity` when the condition is met.

### 4.2 UpdateRequiredActivity

`UpdateRequiredActivity` is included in the module. It:

- Sets the provided app theme.
- Initializes `InAppUpdateManager` with `isUpdateRequired = true`.
- Blocks the back button (calls `finishAffinity` + `exitProcess`).
- Shows an "Update" button that calls `inAppUpdateManager.requireUpdate()`.

Declare it in your `AndroidManifest.xml`:

```xml
<activity
    android:name="com.infomaniak.core.inappupdate.ui.UpdateRequiredActivity"
    android:exported="false" />
```

### 4.3 UpdateRequiredScreen composable

If you want to build your own mandatory-update screen in Compose, use `UpdateRequiredScreen` directly:

```kotlin
@Composable
fun MyUpdateRequiredScreen(onUpdateClick: () -> Unit) {
    UpdateRequiredScreen(
        illustration = painterResource(R.drawable.il_update_required),
        titleTextStyle = MaterialTheme.typography.headlineMedium,
        descriptionTextStyle = MaterialTheme.typography.bodyMedium,
        installUpdateButton = {
            Button(onClick = onUpdateClick) {
                Text(text = stringResource(R.string.updateApp))
            }
        },
    )
}
```

The composable adapts its layout automatically:

- **Compact width** — illustration stacked above the text (portrait phones).
- **Medium / expanded width** — illustration and text side by side (tablets, landscape).

---

## 5. F-Droid flavor

On the `fdroid` flavor, `InAppUpdateManager.checkUpdateIsAvailable()` fetches the latest release from the F-Droid / Infomaniak API and invokes `onFDroidResult` with `true` when `currentVersionCode < latestVersionCode`.

```kotlin
// In your Application or MainActivity:
AppUpdateScheduler(context).scheduleWorkIfNeeded()
```

---

## 6. Customising check frequency

By default the module checks for an update every **20 launches** (`DEFAULT_APP_UPDATE_LAUNCHES = 20`).  
Once the user accepts the update dialog (`IS_USER_WANTING_UPDATES_KEY = true`) the check is performed on every launch until the download is complete.

You can manually write DataStore preferences via:

```kotlin
inAppUpdateManager.set(AppUpdateSettingsRepository.APP_UPDATE_LAUNCHES_KEY, 5)
```

Or reset all update settings to their defaults:

```kotlin
inAppUpdateManager.resetUpdateSettings()
```
