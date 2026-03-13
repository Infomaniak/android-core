# AppLock Module Documentation

The `AppLock` module provides a way to secure access to your application using biometrics (fingerprint, face recognition) or device credentials (PIN, pattern, password).

## 1. Integration (Gradle)

Add the following dependency to your `build.gradle.kts` file:

```kotlin
implementation(project(":Core:AppLock"))
```
or with a composite build
```kotlin
implementation(core.infomaniak.core.applock)
```

## 2. Global Configuration

To enable automatic locking, you should call `AppLockManager.scheduleLockIfNeeded` in your main activity or a base activity. Your activity must inherit from `FragmentActivity`.

### Example in `MainActivity`:

```kotlin
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure App Lock
        AppLockManager.scheduleLockIfNeeded(
            targetActivity = this,
            lockActivityCls = MyCustomLockActivity::class.java, // Your lock screen implementation
            isAppLockEnabled = { 
                // Logic to check if the user enabled the feature in settings
                settingsRepository.isAppLockEnabled()
            },
            autoLockTimeout = 1.minutes // Optional, defaults to 1 minute
        )
    }
}
```

---

## 3. Implementing the Lock Screen

You must create your own lock activity that inherits from `BaseAppLockActivity`. You have two main options:

### A. Jetpack Compose Version

Inherit from `AppLockComposeActivity` and implement the `@Composable Content()` function.

```kotlin
class MyCustomLockActivity : AppLockComposeActivity() {
    @Composable
    override fun Content() {
        // Your Compose UI
        AppLockScreenContent(
            onUnlockClick = {
                // Credentials request is automatically triggered on start,
                // but you can trigger it manually via a button.
                requestCredentials { onCredentialsSuccessful() }
            }
        )
    }
}
```

### B. View Version (Legacy)

Inherit from `AppLockViewActivity`. You need to provide a primary color. This activity uses a default layout (`activity_lock.xml`).

```kotlin
class MyCustomLockActivity : AppLockViewActivity() {
    // Sets the background color of the unlock button
    override val primaryColor: Int = Color.BLUE 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Binding and click logic are already handled by AppLockViewActivity
    }
}
```

### C. Manifest Declaration

Don't forget to declare your custom activity in your `AndroidManifest.xml`. It is recommended to use `launchMode="singleTop"` or `launchMode="singleInstance"` to avoid multiple instances of the lock screen.

```xml
<activity
    android:name=".ui.MyCustomLockActivity"
    android:exported="false"
    android:launchMode="singleTop" />
```

---

## 4. Helper Utilities

### Check Biometric Availability
Before showing the option to the user in your settings, check if the device supports it:

```kotlin
if (AppLockManager.hasBiometrics()) {
    // Show "Enable App Lock" switch
}
```

### Requesting Credentials Manually
If you need to prompt for biometric authentication for a specific sensitive action:

```kotlin
fragmentActivity.requestCredentials {
    // Action performed successfully after authentication
}
```

---

## 5. How It Works

- **Auto-Lock Timeout**: The app automatically locks if it remains in the background longer than the duration defined in `autoLockTimeout`.
- **Screen Off**: If the screen is turned off while the app is in the foreground, it will be locked upon next use.
- **Security**: Authentication uses `BIOMETRIC_WEAK` or `DEVICE_CREDENTIAL`, allowing users to use their device's fallback method (PIN/Pattern) if biometrics fail or are not configured.
